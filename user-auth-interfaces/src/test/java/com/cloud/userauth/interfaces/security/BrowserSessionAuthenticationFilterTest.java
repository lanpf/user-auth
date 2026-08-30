package com.cloud.userauth.interfaces.security;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cloud.userauth.application.authentication.AuthenticatedSession;
import com.cloud.userauth.application.port.BrowserSessionStore;
import com.cloud.userauth.domain.authentication.session.SessionId;
import jakarta.servlet.http.Cookie;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

class BrowserSessionAuthenticationFilterTest {
    private static final AuthenticatedSession AUTHENTICATED_SESSION =
            new AuthenticatedSession(1001L, 2001L, new SessionId("session-1"));

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAuthenticateAndRenewBrowserSessionFromCookie() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(BrowserSessionCookie.COOKIE_NAME, "browser-token"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        new BrowserSessionAuthenticationFilter(new StubBrowserSessionStore()).doFilter(
                request, response, new MockFilterChain());

        assertInstanceOf(
                AuthenticatedSession.class,
                SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(authority -> SessionAuthenticationAuthority.BROWSER_SESSION.getValue().equals(
                        authority.getAuthority())));
        String setCookie = response.getHeader("Set-Cookie");
        assertTrue(setCookie != null && setCookie.contains("BROWSER_SESSION=browser-token"));
    }

    private static final class StubBrowserSessionStore implements BrowserSessionStore {
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
            return "browser-token".equals(credential)
                    ? Optional.of(new ResolvedSession(
                            AUTHENTICATED_SESSION,
                            Duration.ofMinutes(30),
                            true))
                    : Optional.empty();
        }

        @Override
        public void revoke(SessionId loginSessionId) {
            throw new UnsupportedOperationException();
        }
    }
}
