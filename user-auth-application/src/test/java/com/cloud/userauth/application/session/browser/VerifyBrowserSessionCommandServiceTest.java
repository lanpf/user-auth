package com.cloud.userauth.application.session.browser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cloud.userauth.application.authentication.AuthenticatedSession;
import com.cloud.userauth.application.port.BrowserSessionStore;
import com.cloud.userauth.domain.authentication.account.AuthAccountId;
import com.cloud.userauth.domain.authentication.session.SessionId;
import com.cloud.userauth.domain.user.UserId;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class VerifyBrowserSessionCommandServiceTest {

    @Test
    void shouldReturnVerifiedBrowserSession() {
        AuthenticatedSession authenticatedSession = new AuthenticatedSession(
                new UserId(1001L),
                new AuthAccountId(2002L),
                new SessionId("session-1"));
        VerifyBrowserSessionCommandService service = new VerifyBrowserSessionCommandService(
                new Sessions(Map.of("browser-token", new BrowserSessionStore.ResolvedSession(
                        authenticatedSession, Duration.ofMinutes(30)))));

        VerifyBrowserSessionOutput output = service.execute(
                new VerifyBrowserSessionCommand("browser-token"));

        assertEquals(new VerifyBrowserSessionOutput.Verified(
                new UserId(1001L),
                new SessionId("session-1"),
                Duration.ofMinutes(30)), output);
    }

    @Test
    void shouldReturnUnverifiedForUnknownCredential() {
        VerifyBrowserSessionCommandService service =
                new VerifyBrowserSessionCommandService(new Sessions(Map.of()));

        VerifyBrowserSessionOutput output = service.execute(
                new VerifyBrowserSessionCommand("missing"));

        assertEquals(new VerifyBrowserSessionOutput.Unverified(), output);
    }

    @Test
    void shouldPropagateStoreFailureInsteadOfReturningUnverified() {
        VerifyBrowserSessionCommandService service =
                new VerifyBrowserSessionCommandService(new UnavailableSessions());

        assertThrows(IllegalStateException.class, () -> service.execute(
                new VerifyBrowserSessionCommand("browser-token")));
    }

    private record Sessions(Map<String, BrowserSessionStore.ResolvedSession> sessions)
            implements BrowserSessionStore {

        @Override
        public CreatedSession create(
                AuthenticatedSession authenticatedSession,
                Duration idleTtl,
                Duration absoluteTtl
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<ResolvedSession> find(String credential) {
            return Optional.ofNullable(sessions.get(credential));
        }

        @Override
        public boolean end(String credential) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void revoke(SessionId loginSessionId) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class UnavailableSessions implements BrowserSessionStore {

        @Override
        public CreatedSession create(
                AuthenticatedSession authenticatedSession,
                Duration idleTtl,
                Duration absoluteTtl
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<ResolvedSession> find(String credential) {
            throw new IllegalStateException("session store unavailable");
        }

        @Override
        public boolean end(String credential) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void revoke(SessionId loginSessionId) {
            throw new UnsupportedOperationException();
        }
    }
}
