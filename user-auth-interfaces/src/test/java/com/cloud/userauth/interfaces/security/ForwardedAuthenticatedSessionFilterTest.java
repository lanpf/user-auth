package com.cloud.userauth.interfaces.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cloud.framework.core.RequestHeader;
import com.cloud.framework.core.SubjectType;
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
    void shouldAuthenticateHostSessionWithSubjectType() throws Exception {
        Authentication authentication = authenticate(SubjectType.HOST_SESSION);

        assertEquals(
                new ForwardedAuthenticatedSessionFilter.ForwardedAuthenticatedSession(
                        "1001", "session-1"),
                authentication.getPrincipal());
        assertEquals(SessionAuthenticationAuthority.HOST_SESSION.getValue(),
                authentication.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void shouldAuthenticateBrowserSessionWithRestrictedAuthority() throws Exception {
        Authentication authentication = authenticate(SubjectType.BROWSER_SESSION);

        assertEquals(
                new ForwardedAuthenticatedSessionFilter.ForwardedAuthenticatedSession(
                        "1001", "session-1"),
                authentication.getPrincipal());
        assertEquals(SessionAuthenticationAuthority.BROWSER_SESSION.getValue(),
                authentication.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void shouldNotAuthenticateWithoutSubjectType() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(RequestHeader.USER_ID, "1001");
        request.addHeader(RequestHeader.SESSION_ID, "session-1");

        new ForwardedAuthenticatedSessionFilter().doFilter(
                request, new MockHttpServletResponse(), noOpChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldNotAuthenticateUnknownSubjectType() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(RequestHeader.SUBJECT_TYPE, "UNKNOWN_SESSION");
        request.addHeader(RequestHeader.USER_ID, "1001");
        request.addHeader(RequestHeader.SESSION_ID, "session-1");

        new ForwardedAuthenticatedSessionFilter().doFilter(
                request, new MockHttpServletResponse(), noOpChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldNotAuthenticatePartialForwardedSessionContext() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(RequestHeader.SUBJECT_TYPE, SubjectType.HOST_SESSION.name());
        request.addHeader(RequestHeader.USER_ID, "1001");

        new ForwardedAuthenticatedSessionFilter().doFilter(
                request, new MockHttpServletResponse(), noOpChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    private static Authentication authenticate(SubjectType subjectType) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(RequestHeader.SUBJECT_TYPE, subjectType.name());
        request.addHeader(RequestHeader.USER_ID, "1001");
        request.addHeader(RequestHeader.SESSION_ID, "session-1");

        new ForwardedAuthenticatedSessionFilter().doFilter(
                request, new MockHttpServletResponse(), noOpChain());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertTrue(authentication != null && authentication.isAuthenticated());
        return authentication;
    }

    private static FilterChain noOpChain() {
        return (ignoredRequest, ignoredResponse) -> { };
    }
}
