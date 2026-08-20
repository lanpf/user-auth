package com.cloud.userauth.application.session.handoff;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cloud.userauth.application.authentication.AuthenticatedSession;
import com.cloud.userauth.application.common.ApplicationException;
import com.cloud.userauth.application.port.SessionHandoffStore;
import com.cloud.userauth.domain.authentication.account.AuthAccountId;
import com.cloud.userauth.domain.authentication.credential.CredentialId;
import com.cloud.userauth.domain.authentication.session.Client;
import com.cloud.userauth.domain.authentication.session.Device;
import com.cloud.userauth.domain.authentication.session.LoginScene;
import com.cloud.userauth.domain.authentication.session.LoginSession;
import com.cloud.userauth.domain.authentication.session.LoginSessionRepository;
import com.cloud.userauth.domain.authentication.session.SessionId;
import com.cloud.userauth.domain.user.UserId;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class SessionHandoffServiceTest {
    private static final Instant NOW = Instant.parse("2026-08-11T00:00:00Z");
    private static final AuthenticatedSession AUTHENTICATED_SESSION =
            new AuthenticatedSession(1L, 2L, new SessionId("s1"));

    @Test
    void shouldIssueTicketBoundToTargetSessionType() {
        RecordingStore store = new RecordingStore();
        SessionHandoffService service = service(store, activeSession());

        IssuedSessionHandoff handoff = service.issue(
                AUTHENTICATED_SESSION.userId(), AUTHENTICATED_SESSION.sessionId().value(),
                SessionHandoffTarget.BROWSER_SESSION);

        assertEquals("ticket", handoff.ticket());
        assertEquals(SessionHandoffTarget.BROWSER_SESSION, store.issuedTarget);
        assertEquals(Duration.ofSeconds(60), store.issuedTtl);
    }

    @Test
    void shouldConsumeTicketForExpectedTargetSessionType() {
        SessionHandoffService service = new SessionHandoffService(
                new FixedStore(SessionHandoffTarget.BROWSER_SESSION),
                new LoginSessions(activeSession()),
                Clock.fixed(NOW, ZoneOffset.UTC),
                Duration.ofSeconds(60));

        ConsumedSessionHandoff handoff = service.consume(
                "ticket", SessionHandoffTarget.BROWSER_SESSION);

        assertEquals(AUTHENTICATED_SESSION, handoff.authenticatedSession());
        assertEquals("handoff-id", handoff.handoffId());
    }

    @Test
    void shouldRejectTicketWithoutExpectedTargetSessionBinding() {
        SessionHandoffService service = new SessionHandoffService(
                new FixedStore(null),
                new LoginSessions(activeSession()),
                Clock.fixed(NOW, ZoneOffset.UTC),
                Duration.ofSeconds(60));

        assertThrows(ApplicationException.class, () -> service.consume(
                "ticket", SessionHandoffTarget.BROWSER_SESSION));
    }

    @Test
    void shouldRejectConsumedTicketWhenLoginSessionIsRevoked() {
        LoginSession revokedSession = activeSession();
        revokedSession.revoke(NOW.minusSeconds(1));
        SessionHandoffService service = service(
                new FixedStore(SessionHandoffTarget.BROWSER_SESSION), revokedSession);

        assertThrows(ApplicationException.class, () -> service.consume(
                "ticket", SessionHandoffTarget.BROWSER_SESSION));
    }

    @Test
    void shouldLimitTicketTtlToRemainingLoginSessionLifetime() {
        RecordingStore store = new RecordingStore();
        LoginSession loginSession = activeSession(NOW.plusSeconds(20));
        SessionHandoffService service = service(store, loginSession);

        service.issue(AUTHENTICATED_SESSION.userId(), AUTHENTICATED_SESSION.sessionId().value(),
                SessionHandoffTarget.BROWSER_SESSION);

        assertEquals(Duration.ofSeconds(20), store.issuedTtl);
    }

    private static final class RecordingStore implements SessionHandoffStore {
        private SessionHandoffTarget issuedTarget;
        private Duration issuedTtl;

        @Override
        public IssuedTicket issue(
                AuthenticatedSession authenticatedSession,
                String handoffId,
                SessionHandoffTarget target,
                Duration ttl
        ) {
            issuedTarget = target;
            issuedTtl = ttl;
            return new IssuedTicket(handoffId, "ticket");
        }

        @Override
        public Optional<ConsumedTicket> consume(String ticket) {
            return Optional.empty();
        }

        @Override
        public void revoke(SessionId loginSessionId) {
        }
    }

    private record FixedStore(
            SessionHandoffTarget target
    ) implements SessionHandoffStore {
        @Override
        public IssuedTicket issue(
                AuthenticatedSession authenticatedSession,
                String handoffId,
                SessionHandoffTarget issuedTarget,
                Duration ttl
        ) {
            return new IssuedTicket(handoffId, "ticket");
        }

        @Override
        public Optional<ConsumedTicket> consume(String ticket) {
            return Optional.of(new ConsumedTicket(
                    AUTHENTICATED_SESSION, "handoff-id", target));
        }

        @Override
        public void revoke(SessionId loginSessionId) {
        }
    }

    private static SessionHandoffService service(
            SessionHandoffStore store,
            LoginSession loginSession
    ) {
        return new SessionHandoffService(
                store,
                new LoginSessions(loginSession),
                Clock.fixed(NOW, ZoneOffset.UTC),
                Duration.ofSeconds(60));
    }

    private static LoginSession activeSession() {
        return activeSession(NOW.plusSeconds(3600));
    }

    private static LoginSession activeSession(Instant expiresAt) {
        return LoginSession.create(
                AUTHENTICATED_SESSION.sessionId(),
                new UserId(AUTHENTICATED_SESSION.userId()),
                new AuthAccountId(AUTHENTICATED_SESSION.authAccountId()),
                new CredentialId(3L),
                LoginScene.MOBILE_LOGIN,
                new Device("device", "PHONE", "phone"),
                new Client("app", "IOS", "1.0"),
                NOW.minusSeconds(60),
                expiresAt);
    }

    private record LoginSessions(LoginSession session) implements LoginSessionRepository {
        @Override
        public SessionId nextId() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void save(LoginSession value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<LoginSession> findById(SessionId id) {
            return session.id().equals(id) ? Optional.of(session) : Optional.empty();
        }

        @Override
        public List<LoginSession> findActiveByUserId(UserId userId) {
            return List.of();
        }

        @Override
        public List<LoginSession> findActiveByUserIdAndDevice(UserId userId, String deviceId) {
            return List.of();
        }
    }
}
