package com.cloud.userauth.application.session.handoff;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.cloud.userauth.application.authentication.AuthenticatedSession;
import com.cloud.userauth.application.port.H5SessionStore;
import com.cloud.userauth.application.port.SessionHandoffTicketStore;
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

class H5SessionHandoffCommandServiceTest {
    private static final Instant NOW = Instant.parse("2026-08-11T00:00:00Z");

    @Test
    void shouldConsumeH5TicketAndCreateH5Session() {
        AuthenticatedSession authenticatedSession =
                new AuthenticatedSession(1L, 2L, new SessionId("s1"));
        SessionHandoffTicketService ticketService = new SessionHandoffTicketService(
                new TicketStore(authenticatedSession),
                new Sessions(activeSession(authenticatedSession.sessionId())),
                Clock.fixed(NOW, ZoneOffset.UTC),
                Duration.ofSeconds(60));
        H5SessionHandoffCommandService service = new H5SessionHandoffCommandService(
                ticketService,
                new H5Sessions(),
                Duration.ofMinutes(30),
                Duration.ofHours(8));

        ExchangeH5SessionHandoffCommandOutput output = service.exchange("ticket");

        assertEquals("h5", output.sessionCredential());
        assertEquals(1800, output.sessionTtl().toSeconds());
    }

    private record TicketStore(
            AuthenticatedSession authenticatedSession
    ) implements SessionHandoffTicketStore {
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
                    authenticatedSession, "id", SessionHandoffTarget.H5_SESSION));
        }

        @Override
        public void revoke(SessionId loginSessionId) {
        }
    }

    private static final class H5Sessions implements H5SessionStore {
        @Override
        public H5Session create(
                AuthenticatedSession authenticatedSession,
                Duration idle,
                Duration absolute
        ) {
            return new H5Session("h5", idle);
        }

        @Override
        public Optional<ResolvedH5Session> find(String credential) {
            return Optional.empty();
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

    private record Sessions(LoginSession session) implements LoginSessionRepository {
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
