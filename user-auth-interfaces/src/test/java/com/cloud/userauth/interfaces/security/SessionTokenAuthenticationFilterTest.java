package com.cloud.userauth.interfaces.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cloud.userauth.application.authentication.AuthenticatedSession;
import com.cloud.userauth.application.port.SessionTokenStore;
import com.cloud.userauth.domain.authentication.session.SessionId;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

class SessionTokenAuthenticationFilterTest {
    private static final AuthenticatedSession AUTHENTICATED_SESSION =
            new AuthenticatedSession(1001L, 2001L, new SessionId("session-1"));

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAuthenticateSessionTokenFromHeader() throws Exception {
        StubSessionTokenStore store = new StubSessionTokenStore();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(SessionTokenAuthenticationFilter.SESSION_TOKEN_HEADER, "session-token");

        new SessionTokenAuthenticationFilter(store).doFilter(
                request, new MockHttpServletResponse(), new MockFilterChain());

        assertEquals(1, store.sessionTokenLookupCount);
        assertEquals(AUTHENTICATED_SESSION, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(authority -> SessionAuthenticationAuthorities.HOST_SESSION.equals(
                        authority.getAuthority())));
    }

    @Test
    void shouldIgnoreLegacySessionTokenCookie() throws Exception {
        StubSessionTokenStore store = new StubSessionTokenStore();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new jakarta.servlet.http.Cookie("SESSION_TOKEN", "session-token"));

        new SessionTokenAuthenticationFilter(store).doFilter(
                request, new MockHttpServletResponse(), new MockFilterChain());

        assertEquals(0, store.sessionTokenLookupCount);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    private static final class StubSessionTokenStore implements SessionTokenStore {
        private int sessionTokenLookupCount;

        @Override
        public IssuedSessionToken create(AuthenticatedSession authenticatedSession, Duration ttl) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<AuthenticatedSession> findAuthenticatedSession(String credential) {
            sessionTokenLookupCount++;
            return "session-token".equals(credential) ? Optional.of(AUTHENTICATED_SESSION) : Optional.empty();
        }

        @Override
        public void revokeByLoginSessionId(SessionId loginSessionId) {
        }
    }
}
