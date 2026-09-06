package com.cloud.userauth.application.session.browser;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cloud.userauth.application.authentication.AuthenticatedSession;
import com.cloud.userauth.application.common.ApplicationException;
import com.cloud.userauth.application.port.BrowserSessionStore;
import com.cloud.userauth.domain.authentication.account.AuthAccountId;
import com.cloud.userauth.domain.authentication.session.SessionId;
import com.cloud.userauth.domain.user.UserId;
import java.time.Duration;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class EndBrowserSessionCommandServiceTest {
    private static final AuthenticatedSession SESSION =
            new AuthenticatedSession(
                    new UserId(1001L), new AuthAccountId(2002L), new SessionId("session-1"));

    @Test
    void shouldEndMatchingBrowserSession() {
        Stores store = new Stores(SESSION);
        new EndBrowserSessionCommandService(store)
                .execute(new EndBrowserSessionCommand(
                        new UserId(1001L), new SessionId("session-1"), "credential-1"));

        assertTrue(store.ended.contains("credential-1"));
    }

    @Test
    void shouldNotEndSessionOwnedByAnotherUser() {
        Stores store = new Stores(SESSION);

        assertThrows(ApplicationException.class, () -> new EndBrowserSessionCommandService(store)
                .execute(new EndBrowserSessionCommand(
                        new UserId(9999L), new SessionId("session-1"), "credential-1")));

        assertTrue(store.ended.isEmpty());
    }

    @Test
    void shouldNotEndSessionOfAnotherLoginSession() {
        Stores store = new Stores(SESSION);

        assertThrows(ApplicationException.class, () -> new EndBrowserSessionCommandService(store)
                .execute(new EndBrowserSessionCommand(
                        new UserId(1001L), new SessionId("session-other"), "credential-1")));

        assertTrue(store.ended.isEmpty());
    }

    @Test
    void shouldRejectUnknownCredential() {
        Stores store = new Stores(null);

        assertThrows(ApplicationException.class, () -> new EndBrowserSessionCommandService(store)
                .execute(new EndBrowserSessionCommand(
                        new UserId(1001L), new SessionId("session-1"), "missing")));

        assertTrue(store.ended.isEmpty());
    }

    private static final class Stores implements BrowserSessionStore {
        private final AuthenticatedSession stored;
        private final Set<String> ended = new HashSet<>();

        private Stores(AuthenticatedSession stored) {
            this.stored = stored;
        }

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
            return stored == null
                    ? Optional.empty()
                    : Optional.of(new ResolvedSession(stored, Duration.ofMinutes(30)));
        }

        @Override
        public boolean end(String credential) {
            return ended.add(credential);
        }

        @Override
        public void revoke(SessionId loginSessionId) {
            throw new UnsupportedOperationException();
        }
    }
}
