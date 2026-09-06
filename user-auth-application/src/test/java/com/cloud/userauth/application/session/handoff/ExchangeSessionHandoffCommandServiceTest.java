package com.cloud.userauth.application.session.handoff;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.cloud.userauth.application.authentication.AuthenticatedSession;
import com.cloud.userauth.application.port.BrowserSessionStore;
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

class ExchangeSessionHandoffCommandServiceTest {
    private static final Instant NOW = Instant.parse("2026-08-11T00:00:00Z");

    @Test
    void shouldConsumeTicketAndCreateBrowserSession() {
        AuthenticatedSession authenticatedSession =
                new AuthenticatedSession(new UserId(1L), new AuthAccountId(2L), new SessionId("s1"));
        SessionHandoffService handoffService = new SessionHandoffService(
                new Store(authenticatedSession),
                new LoginSessions(activeSession(authenticatedSession.sessionId())),
                Clock.fixed(NOW, ZoneOffset.UTC),
                Duration.ofSeconds(60));
        ExchangeSessionHandoffCommandService service = new ExchangeSessionHandoffCommandService(
                handoffService,
                new Sessions(),
                Duration.ofMinutes(30),
                Duration.ofHours(8));

        ExchangeSessionHandoffOutput output = service.execute(new ExchangeSessionHandoffCommand(
                "ticket",
                SessionHandoffTarget.BROWSER_SESSION
        ));

        assertEquals("browser-session", output.sessionCredential());
        assertEquals(28800, output.sessionTtl().toSeconds());
    }

    private record Store(
            AuthenticatedSession authenticatedSession
    ) implements SessionHandoffStore {
        @Override
        public IssuedTicket issue(
                AuthenticatedSession ignored,
                String id,
                SessionHandoffTarget target,
                Duration ttl
        ) {
            return new IssuedTicket(id, "ticket");
        }

        @Override
        public Optional<ConsumedTicket> consume(String ticket) {
            return Optional.of(new ConsumedTicket(
                    authenticatedSession, "id", SessionHandoffTarget.BROWSER_SESSION));
        }

        @Override
        public void revoke(SessionId loginSessionId) {
        }
    }

    private static final class Sessions implements BrowserSessionStore {
        @Override
        public CreatedSession create(
                AuthenticatedSession authenticatedSession,
                Duration idle,
                Duration absolute
        ) {
            return new CreatedSession("browser-session", absolute);
        }

        @Override
        public Optional<ResolvedSession> find(String credential) {
            return Optional.empty();
        }

        @Override
        public boolean end(String credential) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void revoke(SessionId loginSessionId) {
        }
    }

    private static LoginSession activeSession(SessionId sessionId) {
        return LoginSession.create(
                sessionId,
                new UserId(1L),
                new AuthAccountId(2L),
                new CredentialId(3L),
                LoginScene.MOBILE_LOGIN,
                new Device("device", "PHONE", "phone"),
                new Client("app", "IOS", "1.0"),
                NOW.minusSeconds(60),
                NOW.plusSeconds(3600));
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
