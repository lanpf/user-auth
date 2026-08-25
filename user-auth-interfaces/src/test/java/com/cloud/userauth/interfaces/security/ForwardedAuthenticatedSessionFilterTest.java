package com.cloud.userauth.interfaces.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cloud.framework.core.RequestHeader;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

class ForwardedAuthenticatedSessionFilterTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAuthenticateCompleteForwardedSessionContext() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(RequestHeader.USER_ID, "1001");
        request.addHeader(RequestHeader.SESSION_ID, "session-1");
        FilterChain chain = (ignoredRequest, ignoredResponse) -> { };

        new ForwardedAuthenticatedSessionFilter().doFilter(
                request, new MockHttpServletResponse(), chain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertTrue(authentication.isAuthenticated());
        assertEquals(
                new ForwardedAuthenticatedSessionFilter.ForwardedAuthenticatedSession(
                        "1001", "session-1"),
                authentication.getPrincipal());
        assertEquals(SessionAuthenticationAuthorities.HOST_SESSION,
                authentication.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void shouldNotAuthenticatePartialForwardedSessionContext() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(RequestHeader.USER_ID, "1001");

        new ForwardedAuthenticatedSessionFilter().doFilter(
                request, new MockHttpServletResponse(),
                (ignoredRequest, ignoredResponse) -> { });

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
