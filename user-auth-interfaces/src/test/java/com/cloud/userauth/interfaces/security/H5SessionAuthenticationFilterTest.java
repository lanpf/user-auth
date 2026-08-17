package com.cloud.userauth.interfaces.security;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cloud.userauth.application.authentication.AuthenticatedSession;
import com.cloud.userauth.application.port.H5SessionStore;
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

class H5SessionAuthenticationFilterTest {
    private static final AuthenticatedSession AUTHENTICATED_SESSION =
            new AuthenticatedSession(1001L, 2001L, new SessionId("session-1"));

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAuthenticateAndRenewH5SessionFromCookie() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(H5SessionCookie.NAME, "h5-token"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        new H5SessionAuthenticationFilter(new StubH5SessionStore()).doFilter(
                request, response, new MockFilterChain());

        assertInstanceOf(
                AuthenticatedSession.class,
                SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(authority -> SessionAuthenticationAuthorities.H5_SESSION.equals(
                        authority.getAuthority())));
        String setCookie = response.getHeader("Set-Cookie");
        assertTrue(setCookie != null && setCookie.contains("H5_SESSION=h5-token"));
    }

    private static final class StubH5SessionStore implements H5SessionStore {
        @Override
        public H5Session create(
                AuthenticatedSession authenticatedSession,
                Duration idleTtl,
                Duration absoluteTtl
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<ResolvedH5Session> find(String credential) {
            return "h5-token".equals(credential)
                    ? Optional.of(new ResolvedH5Session(
                            AUTHENTICATED_SESSION,
                            Duration.ofMinutes(30),
                            true))
                    : Optional.empty();
        }

        @Override
        public void revokeByLoginSessionId(SessionId loginSessionId) {
            throw new UnsupportedOperationException();
        }
    }
}
