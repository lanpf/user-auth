package com.cloud.userauth.infrastructure.session.redis;

import com.cloud.userauth.application.authentication.AuthenticatedSession;
import com.cloud.userauth.application.port.H5SessionStore;
import com.cloud.userauth.domain.authentication.session.LoginSession;
import com.cloud.userauth.domain.authentication.session.LoginSessionRepository;
import com.cloud.userauth.domain.authentication.session.SessionId;
import com.cloud.userauth.domain.authentication.session.SessionStatus;
import com.cloud.userauth.infrastructure.session.redis.config.H5SessionProperties;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

@RequiredArgsConstructor
public final class RedisH5SessionStore implements H5SessionStore {
    private static final String KEY_PREFIX = "user-auth:h5-session:";
    private static final String PARENT_KEY_PREFIX = "user-auth:h5-session:parent:";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final H5SessionProperties properties;
    private final StringRedisTemplate redisTemplate;
    private final LoginSessionRepository loginSessionRepository;
    private final Clock clock;

    @Override
    public H5Session create(
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
                KEY_PREFIX + credential,
                serialize(authenticatedSession, absoluteExpiresAt),
                effectiveIdleTtl);
        String parentKey = PARENT_KEY_PREFIX + authenticatedSession.sessionId().value();
        redisTemplate.opsForSet().add(parentKey, credential);
        redisTemplate.expire(parentKey, effectiveAbsoluteTtl);
        return new H5Session(credential, effectiveIdleTtl);
    }

    @Override
    public Optional<ResolvedH5Session> find(String credential) {
        if (credential == null || credential.isBlank()) {
            return Optional.empty();
        }
        String key = KEY_PREFIX + credential;
        return Optional.ofNullable(redisTemplate.opsForValue().get(key))
                .flatMap(RedisH5SessionStore::parse)
                .filter(data -> data.absoluteExpiresAt().isAfter(clock.instant()))
                .filter(data -> active(data.authenticatedSession()))
                .map(data -> refresh(key, data));
    }

    @Override
    public void revoke(SessionId loginSessionId) {
        String parentKey = PARENT_KEY_PREFIX + loginSessionId.value();
        Set<String> credentials = redisTemplate.opsForSet().members(parentKey);
        if (credentials != null && !credentials.isEmpty()) {
            redisTemplate.delete(credentials.stream()
                    .map(credential -> KEY_PREFIX + credential)
                    .toList());
        }
        redisTemplate.delete(parentKey);
    }

    private Instant activeParentExpiresAt(
            AuthenticatedSession authenticatedSession,
            Instant now
    ) {
        return loginSessionRepository.findById(authenticatedSession.sessionId())
                .filter(session -> session.getStatus() == SessionStatus.ACTIVE)
                .filter(session -> session.getUserId().value().equals(authenticatedSession.userId()))
                .filter(session -> session.getAuthAccountId().value()
                        .equals(authenticatedSession.authAccountId()))
                .filter(session -> session.getExpiresAt().isAfter(now))
                .map(LoginSession::getExpiresAt)
                .orElseThrow(() -> new IllegalStateException(
                        "Cannot create H5 session for an inactive login session"));
    }

    private ResolvedH5Session refresh(String key, H5SessionData data) {
        Duration untilAbsoluteExpiry = Duration.between(clock.instant(), data.absoluteExpiresAt());
        Duration nextIdleTtl = untilAbsoluteExpiry.compareTo(properties.getIdleTtl()) < 0
                ? untilAbsoluteExpiry
                : properties.getIdleTtl();
        long currentTtlSeconds = redisTemplate.getExpire(key);
        boolean renewed = currentTtlSeconds <= properties.getRenewalThreshold().toSeconds();
        if (renewed) {
            redisTemplate.expire(key, nextIdleTtl);
        }
        Duration remainingIdleTtl = renewed
                ? nextIdleTtl
                : Duration.ofSeconds(currentTtlSeconds);
        return new ResolvedH5Session(
                data.authenticatedSession(), remainingIdleTtl, renewed);
    }

    private boolean active(AuthenticatedSession authenticatedSession) {
        return loginSessionRepository.findById(authenticatedSession.sessionId())
                .filter(session -> session.getStatus() == SessionStatus.ACTIVE)
                .filter(session -> session.getExpiresAt().isAfter(clock.instant()))
                .isPresent();
    }

    private static String serialize(
            AuthenticatedSession authenticatedSession,
            Instant absoluteExpiresAt
    ) {
        return authenticatedSession.sessionId().value() + ":"
                + authenticatedSession.userId() + ":"
                + authenticatedSession.authAccountId() + ":"
                + absoluteExpiresAt.toEpochMilli();
    }

    private static Optional<H5SessionData> parse(String value) {
        String[] parts = value.split(":", -1);
        if (parts.length != 4) {
            return Optional.empty();
        }
        try {
            return Optional.of(new H5SessionData(
                    new AuthenticatedSession(
                            Long.valueOf(parts[1]),
                            Long.valueOf(parts[2]),
                            new SessionId(parts[0])),
                    Instant.ofEpochMilli(Long.parseLong(parts[3]))));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    private static String credential() {
        byte[] value = new byte[32];
        RANDOM.nextBytes(value);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    private record H5SessionData(
            AuthenticatedSession authenticatedSession,
            Instant absoluteExpiresAt
    ) {
    }
}
