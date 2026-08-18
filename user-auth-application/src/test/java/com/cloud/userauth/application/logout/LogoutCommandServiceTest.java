package com.cloud.userauth.application.logout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cloud.framework.domain.DomainEvent;
import com.cloud.framework.domain.DomainEventId;
import com.cloud.userauth.domain.authentication.account.AuthAccountId;
import com.cloud.userauth.domain.authentication.credential.CredentialId;
import com.cloud.userauth.domain.authentication.event.UserLoggedOutEvent;
import com.cloud.userauth.domain.authentication.service.SessionDomainService;
import com.cloud.userauth.domain.authentication.session.Client;
import com.cloud.userauth.domain.authentication.session.Device;
import com.cloud.userauth.domain.authentication.session.LoginScene;
import com.cloud.userauth.domain.authentication.session.LoginSession;
import com.cloud.userauth.domain.authentication.session.LoginSessionRepository;
import com.cloud.userauth.domain.authentication.session.SessionId;
import com.cloud.userauth.domain.authentication.session.SessionStatus;
import com.cloud.userauth.domain.user.UserId;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class LogoutCommandServiceTest {
    private static final Instant NOW = Instant.parse("2026-08-02T00:00:00Z");

    @Test
    void shouldRevokeAllSessionArtifactsAndActiveSessionAndPublishLogoutEvent() {
        SessionId sessionId = new SessionId("session-1");
        Sessions sessions = new Sessions(activeSession(sessionId));
        List<DomainEvent> events = new ArrayList<>();
        List<SessionId> revokedArtifacts = new ArrayList<>();
        LogoutCommandService service = new LogoutCommandService(
                sessions,
                List.of(revokedArtifacts::add, revokedArtifacts::add, revokedArtifacts::add),
                new SessionDomainService(() -> new DomainEventId(1L)),
                events::addAll,
                Clock.fixed(NOW, ZoneOffset.UTC));

        LogoutOutput output = service.execute(new LogoutCommand(1001L, sessionId.value()));

        assertEquals(SessionStatus.REVOKED, output.sessionStatus());
        assertEquals(List.of(sessionId, sessionId, sessionId), revokedArtifacts);
        assertEquals(SessionStatus.REVOKED, sessions.session.getStatus());
        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof UserLoggedOutEvent);
    }

    @Test
    void shouldKeepRepeatedLogoutIdempotent() {
        SessionId sessionId = new SessionId("session-1");
        LoginSession session = activeSession(sessionId);
        session.revoke(NOW.minusSeconds(1));
        Sessions sessions = new Sessions(session);
        List<DomainEvent> events = new ArrayList<>();
        LogoutCommandService service = new LogoutCommandService(
                sessions,
                List.of(ignored -> { }),
                new SessionDomainService(() -> new DomainEventId(1L)),
                events::addAll,
                Clock.fixed(NOW, ZoneOffset.UTC));

        LogoutOutput output = service.execute(new LogoutCommand(1001L, sessionId.value()));

        assertEquals(SessionStatus.REVOKED, output.sessionStatus());
        assertTrue(events.isEmpty());
    }

    @Test
    void shouldLogoutWithoutSessionArtifactRevoker() {
        SessionId sessionId = new SessionId("session-1");
        Sessions sessions = new Sessions(activeSession(sessionId));
        List<DomainEvent> events = new ArrayList<>();
        LogoutCommandService service = new LogoutCommandService(
                sessions,
                List.of(),
                new SessionDomainService(() -> new DomainEventId(1L)),
                events::addAll,
                Clock.fixed(NOW, ZoneOffset.UTC));

        LogoutOutput output = service.execute(new LogoutCommand(1001L, sessionId.value()));

        assertEquals(SessionStatus.REVOKED, output.sessionStatus());
        assertEquals(SessionStatus.REVOKED, sessions.session.getStatus());
        assertEquals(1, events.size());
    }

    private static LoginSession activeSession(SessionId sessionId) {
        return LoginSession.create(
                sessionId,
                new UserId(1001L),
                new AuthAccountId(2001L),
                new CredentialId(3001L),
                LoginScene.MOBILE_LOGIN,
                new Device("device-1", "PHONE", "phone"),
                new Client("app", "IOS", "1.0"),
                NOW.minusSeconds(60),
                NOW.plusSeconds(3600));
    }

    private static final class Sessions implements LoginSessionRepository {
        private LoginSession session;

        private Sessions(LoginSession session) {
            this.session = session;
        }

        @Override
        public SessionId nextId() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void save(LoginSession value) {
            session = value;
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
