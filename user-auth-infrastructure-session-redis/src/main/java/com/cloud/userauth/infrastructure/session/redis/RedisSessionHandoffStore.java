package com.cloud.userauth.infrastructure.session.redis;

import com.cloud.userauth.application.authentication.AuthenticatedSession;
import com.cloud.userauth.application.port.SessionHandoffStore;
import com.cloud.userauth.application.session.handoff.SessionHandoffTarget;
import com.cloud.userauth.domain.authentication.session.SessionId;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public final class RedisSessionHandoffStore implements SessionHandoffStore {
    private static final SecureRandom RANDOM = new SecureRandom();

    private final StringRedisTemplate redisTemplate;
    private final RedisSessionHandoffKeyResolver keyResolver;

    @Override
    public IssuedTicket issue(
            AuthenticatedSession authenticatedSession,
            String handoffId,
            SessionHandoffTarget target,
            Duration ttl
    ) {
        String ticket = randomValue();
        redisTemplate.opsForValue().set(
                keyResolver.ticket(ticket),
                serialize(authenticatedSession, handoffId, target),
                ttl);
        String loginSessionKey = keyResolver.loginSession(authenticatedSession.sessionId().value());
        redisTemplate.opsForSet().add(loginSessionKey, ticket);
        redisTemplate.expire(loginSessionKey, ttl);
        return new IssuedTicket(handoffId, ticket);
    }

    @Override
    public Optional<ConsumedTicket> consume(String ticket) {
        if (!StringUtils.hasText(ticket)) {
            return Optional.empty();
        }
        Optional<ConsumedTicket> consumed = parse(
                redisTemplate.opsForValue().getAndDelete(keyResolver.ticket(ticket)));
        consumed.ifPresent(value -> redisTemplate.opsForSet().remove(
                keyResolver.loginSession(value.authenticatedSession().sessionId().value()), ticket));
        return consumed;
    }

    @Override
    public void revoke(SessionId loginSessionId) {
        String loginSessionKey = keyResolver.loginSession(loginSessionId.value());
        Set<String> tickets = redisTemplate.opsForSet().members(loginSessionKey);
        if (!CollectionUtils.isEmpty(tickets)) {
            redisTemplate.delete(tickets.stream()
                    .map(keyResolver::ticket)
                    .toList());
        }
        redisTemplate.delete(loginSessionKey);
    }

    private static String serialize(
            AuthenticatedSession authenticatedSession,
            String handoffId,
            SessionHandoffTarget target
    ) {
        return authenticatedSession.sessionId().value() + ":"
                + authenticatedSession.userId() + ":"
                + authenticatedSession.authAccountId() + ":"
                + handoffId + ":"
                + target.name();
    }

    private static Optional<ConsumedTicket> parse(String value) {
        if (value == null) {
            return Optional.empty();
        }
        String[] parts = value.split(":", -1);
        if (parts.length != 5) {
            return Optional.empty();
        }
        try {
            return Optional.of(new ConsumedTicket(
                    new AuthenticatedSession(
                            Long.valueOf(parts[1]),
                            Long.valueOf(parts[2]),
                            new SessionId(parts[0])),
                    parts[3],
                    SessionHandoffTarget.valueOf(parts[4])));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private static String randomValue() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
