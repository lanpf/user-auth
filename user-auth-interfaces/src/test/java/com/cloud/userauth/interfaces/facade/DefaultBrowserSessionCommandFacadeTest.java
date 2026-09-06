package com.cloud.userauth.interfaces.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.cloud.framework.core.Result;
import com.cloud.userauth.api.authentication.VerifyBrowserSessionApiCommand;
import com.cloud.userauth.api.authentication.VerifyBrowserSessionApiCommandOutput;
import com.cloud.userauth.application.authentication.AuthenticatedSession;
import com.cloud.userauth.application.port.BrowserSessionStore;
import com.cloud.userauth.application.session.browser.EndBrowserSessionCommandService;
import com.cloud.userauth.application.session.browser.VerifyBrowserSessionCommandService;
import com.cloud.userauth.domain.authentication.account.AuthAccountId;
import com.cloud.userauth.domain.authentication.session.SessionId;
import com.cloud.userauth.domain.user.UserId;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class DefaultBrowserSessionCommandFacadeTest {

    @Test
    void shouldMapVerifiedBrowserSessionToApiOutput() {
        BrowserSessionStore store = new StubBrowserSessionStore(Map.of(
                "browser-token",
                new BrowserSessionStore.ResolvedSession(
                        new AuthenticatedSession(
                                new UserId(1001L),
                                new AuthAccountId(2002L),
                                new SessionId("session-1")),
                        Duration.ofMinutes(30))));
        DefaultBrowserSessionCommandFacade facade = new DefaultBrowserSessionCommandFacade(
                new VerifyBrowserSessionCommandService(store),
                new EndBrowserSessionCommandService(store));

        Result<VerifyBrowserSessionApiCommandOutput> result =
                facade.verify(new VerifyBrowserSessionApiCommand("browser-token"));

        assertEquals(Result.success(VerifyBrowserSessionApiCommandOutput.verified(
                1001L, "session-1", 1800L)), result);
    }

    @Test
    void shouldReturnUnverifiedOutputForUnknownCredential() {
        BrowserSessionStore store = new StubBrowserSessionStore(Map.of());
        DefaultBrowserSessionCommandFacade facade = new DefaultBrowserSessionCommandFacade(
                new VerifyBrowserSessionCommandService(store),
                new EndBrowserSessionCommandService(store));

        Result<VerifyBrowserSessionApiCommandOutput> result =
                facade.verify(new VerifyBrowserSessionApiCommand("missing"));

        assertEquals(Result.success(VerifyBrowserSessionApiCommandOutput.unverified()), result);
    }

    private record StubBrowserSessionStore(Map<String, ResolvedSession> sessions)
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
}
