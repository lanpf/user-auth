package com.cloud.userauth.infrastructure.session.redis;

import com.cloud.userauth.application.authentication.AuthenticatedSession;
import com.cloud.userauth.application.port.SessionTokenStore;
import com.cloud.userauth.domain.authentication.session.LoginSessionRepository;
import com.cloud.userauth.domain.authentication.session.SessionId;
import com.cloud.userauth.domain.authentication.session.SessionStatus;
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
public final class RedisSessionTokenStore implements SessionTokenStore {
    private static final String KEY_PREFIX = "user-auth:session-token:";
    private static final String LOGIN_SESSION_KEY_PREFIX =
            "user-auth:session-token:login-session:";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final StringRedisTemplate redisTemplate;
    private final LoginSessionRepository loginSessionRepository;
    private final Clock clock;

    @Override
    public IssuedSessionToken create(AuthenticatedSession authenticatedSession, Duration ttl) {
        Duration effectiveTtl = effectiveTtl(authenticatedSession, ttl);
        String token = token();
        redisTemplate.opsForValue().set(KEY_PREFIX + token,
                authenticatedSession.sessionId().value() + ":" + authenticatedSession.userId() + ":"
                        + authenticatedSession.authAccountId(), effectiveTtl);
        String loginSessionKey = loginSessionKey(authenticatedSession.sessionId());
        redisTemplate.opsForSet().add(loginSessionKey, token);
        redisTemplate.expire(loginSessionKey, effectiveTtl);
        return new IssuedSessionToken(token, effectiveTtl);
    }

    @Override
    public Optional<AuthenticatedSession> findAuthenticatedSession(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(redisTemplate.opsForValue().get(KEY_PREFIX + token))
                .flatMap(this::parse)
                .filter(this::active);
    }

    @Override
    public void revokeByLoginSessionId(SessionId loginSessionId) {
        String loginSessionKey = loginSessionKey(loginSessionId);
        Set<String> credentials = redisTemplate.opsForSet().members(loginSessionKey);
        if (credentials != null && !credentials.isEmpty()) {
            redisTemplate.delete(credentials.stream()
                    .map(token -> KEY_PREFIX + token)
                    .toList());
        }
        redisTemplate.delete(loginSessionKey);
    }

    private Duration effectiveTtl(
            AuthenticatedSession authenticatedSession,
            Duration requestedTtl
    ) {
        Instant now = clock.instant();
        Duration loginSessionTtl = loginSessionRepository
                .findById(authenticatedSession.sessionId())
                .filter(session -> session.getStatus() == SessionStatus.ACTIVE)
                .filter(session -> session.getUserId().value().equals(authenticatedSession.userId()))
                .filter(session -> session.getAuthAccountId().value()
                        .equals(authenticatedSession.authAccountId()))
                .filter(session -> session.getExpiresAt().isAfter(now))
                .map(session -> Duration.between(now, session.getExpiresAt()))
                .orElseThrow(() -> new IllegalStateException(
                        "Cannot create a session token for an inactive login session"));
        return requestedTtl.compareTo(loginSessionTtl) < 0
                ? requestedTtl
                : loginSessionTtl;
    }

    private boolean active(AuthenticatedSession authenticatedSession) {
        return loginSessionRepository.findById(authenticatedSession.sessionId())
                .filter(session -> session.getStatus() == SessionStatus.ACTIVE)
                .filter(session -> session.getExpiresAt().isAfter(clock.instant()))
                .isPresent();
    }

    private Optional<AuthenticatedSession> parse(String value) {
        String[] parts = value.split(":", -1);
        if (parts.length != 3) {
            return Optional.empty();
        }
        try {
            return Optional.of(new AuthenticatedSession(
                    Long.valueOf(parts[1]), Long.valueOf(parts[2]), new SessionId(parts[0])));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    private static String token() {
        byte[] value = new byte[32];
        RANDOM.nextBytes(value);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    private static String loginSessionKey(SessionId loginSessionId) {
        return LOGIN_SESSION_KEY_PREFIX + loginSessionId.value();
    }
}
