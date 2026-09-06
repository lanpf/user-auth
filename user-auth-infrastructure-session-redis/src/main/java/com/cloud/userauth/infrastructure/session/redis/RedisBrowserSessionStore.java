package com.cloud.userauth.infrastructure.session.redis;

import com.cloud.userauth.application.authentication.AuthenticatedSession;
import com.cloud.userauth.application.port.BrowserSessionStore;
import com.cloud.userauth.domain.authentication.account.AuthAccountId;
import com.cloud.userauth.domain.authentication.session.LoginSession;
import com.cloud.userauth.domain.authentication.session.LoginSessionRepository;
import com.cloud.userauth.domain.authentication.session.SessionId;
import com.cloud.userauth.domain.authentication.session.SessionStatus;
import com.cloud.userauth.domain.user.UserId;
import com.cloud.userauth.infrastructure.session.redis.config.BrowserSessionProperties;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.util.CollectionUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public final class RedisBrowserSessionStore implements BrowserSessionStore {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String RESOLVE_AND_RENEW_SCRIPT = """
            local value = redis.call('GET', KEYS[1])
            if not value or value ~= ARGV[1] then
                return nil
            end
            local currentTtl = redis.call('PTTL', KEYS[1])
            if currentTtl <= 0 then
                return nil
            end
            local renewalThreshold = tonumber(ARGV[2])
            if currentTtl <= renewalThreshold then
                local nextTtl = tonumber(ARGV[3])
                if nextTtl <= 0 or redis.call('PEXPIRE', KEYS[1], nextTtl) ~= 1 then
                    return nil
                end
                return tostring(nextTtl)
            end
            return tostring(currentTtl)
            """;
    private static final DefaultRedisScript<String> RESOLVE_AND_RENEW_REDIS_SCRIPT =
            new DefaultRedisScript<>(RESOLVE_AND_RENEW_SCRIPT, String.class);

    private final BrowserSessionProperties properties;
    private final StringRedisTemplate redisTemplate;
    private final BrowserSessionKeyResolver keyResolver;
    private final LoginSessionRepository loginSessionRepository;
    private final Clock clock;

    @Override
    public CreatedSession create(
            AuthenticatedSession authenticatedSession,
            Duration idleTtl,
            Duration absoluteTtl
    ) {
        Instant now = clock.instant();
        Instant parentExpiresAt = activeParentExpiresAt(authenticatedSession, now);
        Instant configuredAbsoluteExpiresAt = now.plus(absoluteTtl);
        Instant absoluteExpiresAt = configuredAbsoluteExpiresAt.isBefore(parentExpiresAt)
                ? configuredAbsoluteExpiresAt
                : parentExpiresAt;
        Duration effectiveAbsoluteTtl = Duration.between(now, absoluteExpiresAt);
        Duration effectiveIdleTtl = idleTtl.compareTo(effectiveAbsoluteTtl) < 0
                ? idleTtl
                : effectiveAbsoluteTtl;
        String credential = credential();
        redisTemplate.opsForValue().set(
                keyResolver.key(credential),
                serialize(authenticatedSession, absoluteExpiresAt),
                effectiveIdleTtl);
        String parentKey = keyResolver.parent(authenticatedSession.sessionId().value());
        redisTemplate.opsForSet().add(parentKey, credential);
        redisTemplate.expire(parentKey, effectiveAbsoluteTtl);
        return new CreatedSession(credential, effectiveAbsoluteTtl);
    }

    @Override
    public Optional<ResolvedSession> find(String credential) {
        if (!StringUtils.hasText(credential)) {
            return Optional.empty();
        }
        String key = keyResolver.key(credential);
        String serialized = redisTemplate.opsForValue().get(key);
        Instant now = clock.instant();
        return parse(serialized)
                .filter(data -> data.absoluteExpiresAt().isAfter(now))
                .filter(data -> active(data.authenticatedSession(), now))
                .flatMap(data -> refresh(key, serialized, data, now));
    }

    @Override
    public boolean end(String credential) {
        if (!StringUtils.hasText(credential)) {
            return false;
        }
        String key = keyResolver.key(credential);
        String data = redisTemplate.opsForValue().getAndDelete(key);
        if (data == null) {
            return false;
        }
        RedisBrowserSessionStore.parse(data).ifPresent(sessionData ->
                redisTemplate.opsForSet().remove(
                        keyResolver.parent(sessionData.authenticatedSession().sessionId().value()),
                        credential));
        return true;
    }

    @Override
    public void revoke(SessionId loginSessionId) {
        String parentKey = keyResolver.parent(loginSessionId.value());
        Set<String> credentials = redisTemplate.opsForSet().members(parentKey);
        if (!CollectionUtils.isEmpty(credentials)) {
            redisTemplate.delete(credentials.stream()
                    .map(keyResolver::key)
                    .toList());
        }
        redisTemplate.delete(parentKey);
    }

    private Instant activeParentExpiresAt(
            AuthenticatedSession authenticatedSession,
            Instant now
    ) {
        Instant parentExpiresAt = loginSessionRepository.findById(authenticatedSession.sessionId())
                .filter(session -> session.getStatus() == SessionStatus.ACTIVE)
                .filter(session -> session.getUserId().equals(authenticatedSession.userId()))
                .filter(session -> session.getAuthAccountId().equals(authenticatedSession.authAccountId()))
                .filter(session -> session.getExpiresAt().isAfter(now))
                .map(LoginSession::getExpiresAt)
                .orElse(null);
        Assert.state(parentExpiresAt != null,
                "Cannot create browser session for an inactive login session");
        return parentExpiresAt;
    }

    private Optional<ResolvedSession> refresh(
            String key,
            String serialized,
            SessionData data,
            Instant now
    ) {
        Duration untilAbsoluteExpiry = Duration.between(now, data.absoluteExpiresAt());
        Duration nextIdleTtl = untilAbsoluteExpiry.compareTo(properties.getIdleTtl()) < 0
                ? untilAbsoluteExpiry
                : properties.getIdleTtl();
        String result = redisTemplate.execute(
                RESOLVE_AND_RENEW_REDIS_SCRIPT,
                List.of(key),
                serialized,
                String.valueOf(properties.getRenewalThreshold().toMillis()),
                String.valueOf(nextIdleTtl.toMillis()));
        return parseRemainingIdleTtl(result)
                .map(remainingIdleTtl -> new ResolvedSession(
                        data.authenticatedSession(), remainingIdleTtl));
    }

    private boolean active(AuthenticatedSession authenticatedSession, Instant now) {
        return loginSessionRepository.findById(authenticatedSession.sessionId())
                .filter(session -> session.getStatus() == SessionStatus.ACTIVE)
                .filter(session -> session.getExpiresAt().isAfter(now))
                .isPresent();
    }

    private static String serialize(
            AuthenticatedSession authenticatedSession,
            Instant absoluteExpiresAt
    ) {
        return authenticatedSession.sessionId().value() + ":"
                + authenticatedSession.userId().value() + ":"
                + authenticatedSession.authAccountId().value() + ":"
                + absoluteExpiresAt.toEpochMilli();
    }

    private static Optional<SessionData> parse(String value) {
        if (!StringUtils.hasText(value)) {
            return Optional.empty();
        }
        String[] parts = value.split(":", -1);
        if (parts.length != 4) {
            return Optional.empty();
        }
        try {
            return Optional.of(new SessionData(
                    new AuthenticatedSession(
                            new UserId(Long.valueOf(parts[1])),
                            new AuthAccountId(Long.valueOf(parts[2])),
                            new SessionId(parts[0])),
                    Instant.ofEpochMilli(Long.parseLong(parts[3]))));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    private static Optional<Duration> parseRemainingIdleTtl(String value) {
        if (!StringUtils.hasText(value)) {
            return Optional.empty();
        }
        try {
            long remainingTtlMillis = Long.parseLong(value);
            if (remainingTtlMillis <= 0) {
                return Optional.empty();
            }
            return Optional.of(Duration.ofMillis(remainingTtlMillis));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    private static String credential() {
        byte[] value = new byte[32];
        RANDOM.nextBytes(value);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    private record SessionData(
            AuthenticatedSession authenticatedSession,
            Instant absoluteExpiresAt
    ) {
    }
}
