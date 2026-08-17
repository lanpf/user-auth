package com.cloud.userauth.infrastructure.oauth2.redis;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;

@RequiredArgsConstructor
public class StringRedisOAuth2AuthorizationStore implements OAuth2AuthorizationRedisStore {
    private static final String SAVE_SCRIPT = """
            if ARGV[6] == '1' then
                redis.call('DEL', KEYS[4])
            end
            if ARGV[7] == '1' then
                redis.call('DEL', KEYS[5])
            end
            redis.call('PSETEX', KEYS[1], ARGV[2], ARGV[1])
            if tonumber(ARGV[4]) > 0 then
                redis.call('PSETEX', KEYS[2], ARGV[4], ARGV[3])
            end
            if tonumber(ARGV[5]) > 0 then
                redis.call('PSETEX', KEYS[3], ARGV[5], ARGV[3])
            end
            if ARGV[8] == '1' and tonumber(ARGV[9]) > 0 then
                redis.call('PSETEX', KEYS[6], ARGV[9], ARGV[3])
            end
            return 1
            """;
    private static final String REMOVE_SCRIPT = """
            redis.call('DEL', KEYS[1])
            if ARGV[1] == '1' then
                redis.call('DEL', KEYS[2])
            end
            if ARGV[2] == '1' then
                redis.call('DEL', KEYS[3])
            end
            return 1
            """;
    private static final DefaultRedisScript<Long> SAVE_REDIS_SCRIPT =
            new DefaultRedisScript<>(SAVE_SCRIPT, Long.class);
    private static final DefaultRedisScript<Long> REMOVE_REDIS_SCRIPT =
            new DefaultRedisScript<>(REMOVE_SCRIPT, Long.class);

    private final StringRedisTemplate redisTemplate;
    private final OAuth2AuthorizationRedisJsonMapper jsonMapper;
    private final OAuth2AuthorizationRedisKeyResolver keyResolver;
    private final Clock clock;

    @Override
    public void save(OAuth2Authorization authorization) {
        Instant now = clock.instant();
        OAuth2Authorization existing = findById(authorization.getId());
        String oldAccessToken = accessTokenValue(existing);
        String oldRefreshToken = refreshTokenValue(existing);
        String newAccessToken = accessTokenValue(authorization);
        String newRefreshToken = refreshTokenValue(authorization);
        boolean accessTokenChanged = oldAccessToken != null
                && !Objects.equals(oldAccessToken, newAccessToken);
        boolean refreshTokenChanged = oldRefreshToken != null
                && !Objects.equals(oldRefreshToken, newRefreshToken);
        long authorizationTtl = authorizationTtl(authorization, now);
        long accessTokenTtl = tokenTtl(accessTokenExpiresAt(authorization), now);
        long refreshTokenTtl = tokenTtl(refreshTokenExpiresAt(authorization), now);
        long refreshHistoryTtl = refreshTokenChanged
                ? tokenTtl(refreshTokenExpiresAt(existing), now)
                : 0L;

        redisTemplate.execute(
                SAVE_REDIS_SCRIPT,
                List.of(
                        authorizationKey(authorization.getId()),
                        accessTokenKey(newAccessToken),
                        refreshTokenKey(newRefreshToken),
                        accessTokenKey(oldAccessToken),
                        refreshTokenKey(oldRefreshToken),
                        refreshTokenHistoryKey(oldRefreshToken)),
                jsonMapper.write(authorization),
                String.valueOf(authorizationTtl),
                authorization.getId(),
                String.valueOf(accessTokenTtl),
                String.valueOf(refreshTokenTtl),
                flag(accessTokenChanged),
                flag(refreshTokenChanged),
                flag(refreshTokenChanged),
                String.valueOf(refreshHistoryTtl));
    }

    @Override
    public void remove(OAuth2Authorization authorization) {
        String accessToken = accessTokenValue(authorization);
        String refreshToken = refreshTokenValue(authorization);
        redisTemplate.execute(
                REMOVE_REDIS_SCRIPT,
                List.of(
                        authorizationKey(authorization.getId()),
                        accessTokenKey(accessToken),
                        refreshTokenKey(refreshToken)),
                flag(accessToken != null),
                flag(refreshToken != null));
    }

    @Override
    public OAuth2Authorization findById(String authorizationId) {
        String value = redisTemplate.opsForValue().get(authorizationKey(authorizationId));
        return value == null ? null : jsonMapper.read(value);
    }

    @Override
    public OAuth2Authorization findByTokenHash(
            String tokenHash,
            OAuth2TokenType tokenType
    ) {
        String indexKey = OAuth2TokenType.ACCESS_TOKEN.equals(tokenType)
                ? accessTokenKey(tokenHash)
                : refreshTokenKey(tokenHash);
        String authorizationId = redisTemplate.opsForValue().get(indexKey);
        if (authorizationId == null) {
            return null;
        }
        OAuth2Authorization authorization = findById(authorizationId);
        if (authorization == null) {
            redisTemplate.delete(indexKey);
        }
        return authorization;
    }

    @Override
    public Optional<String> findReusedRefreshTokenAuthorizationId(String refreshTokenHash) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(
                refreshTokenHistoryKey(refreshTokenHash)));
    }

    private long authorizationTtl(OAuth2Authorization authorization, Instant now) {
        Instant accessTokenExpiresAt = accessTokenExpiresAt(authorization);
        Instant refreshTokenExpiresAt = refreshTokenExpiresAt(authorization);
        Instant expiresAt;
        if (accessTokenExpiresAt == null) {
            expiresAt = refreshTokenExpiresAt;
        } else if (refreshTokenExpiresAt == null || accessTokenExpiresAt.isAfter(refreshTokenExpiresAt)) {
            expiresAt = accessTokenExpiresAt;
        } else {
            expiresAt = refreshTokenExpiresAt;
        }
        if (expiresAt == null) {
            throw new IllegalArgumentException(
                    "OAuth2 authorization must contain an access token or refresh token");
        }
        return Math.max(1L, tokenTtl(expiresAt, now));
    }

    private static long tokenTtl(Instant expiresAt, Instant now) {
        if (expiresAt == null || !expiresAt.isAfter(now)) {
            return 0L;
        }
        return Duration.between(now, expiresAt).toMillis();
    }

    private String authorizationKey(String authorizationId) {
        return keyResolver.authorization(authorizationId);
    }

    private String accessTokenKey(String tokenHash) {
        return keyResolver.accessToken(tokenHash);
    }

    private String refreshTokenKey(String tokenHash) {
        return keyResolver.refreshToken(tokenHash);
    }

    private String refreshTokenHistoryKey(String tokenHash) {
        return keyResolver.refreshTokenHistory(tokenHash);
    }

    private static String accessTokenValue(OAuth2Authorization authorization) {
        if (authorization == null || authorization.getAccessToken() == null) {
            return null;
        }
        return authorization.getAccessToken().getToken().getTokenValue();
    }

    private static Instant accessTokenExpiresAt(OAuth2Authorization authorization) {
        if (authorization == null || authorization.getAccessToken() == null) {
            return null;
        }
        OAuth2AccessToken token = authorization.getAccessToken().getToken();
        return token.getExpiresAt();
    }

    private static String refreshTokenValue(OAuth2Authorization authorization) {
        if (authorization == null || authorization.getRefreshToken() == null) {
            return null;
        }
        return authorization.getRefreshToken().getToken().getTokenValue();
    }

    private static Instant refreshTokenExpiresAt(OAuth2Authorization authorization) {
        if (authorization == null || authorization.getRefreshToken() == null) {
            return null;
        }
        OAuth2RefreshToken token = authorization.getRefreshToken().getToken();
        return token.getExpiresAt();
    }

    private static String flag(boolean value) {
        return value ? "1" : "0";
    }
}
