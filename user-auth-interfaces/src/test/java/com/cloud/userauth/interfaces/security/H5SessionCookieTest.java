package com.cloud.userauth.interfaces.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.servlet.http.Cookie;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class H5SessionCookieTest {

    @Test
    void shouldReadH5SessionCookie() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(
                new Cookie("OTHER", "other"),
                new Cookie(H5SessionCookie.NAME, "h5-token"));

        assertEquals("h5-token", H5SessionCookie.read(request).orElseThrow());
    }

    @Test
    void shouldWriteSecureHttpOnlyH5SessionCookie() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        H5SessionCookie.write(response, "h5-token", Duration.ofMinutes(30));

        String setCookie = response.getHeader("Set-Cookie");
        assertTrue(setCookie != null && setCookie.contains("H5_SESSION=h5-token"));
        assertTrue(setCookie.contains("Max-Age=1800"));
        assertTrue(setCookie.contains("Secure"));
        assertTrue(setCookie.contains("HttpOnly"));
        assertTrue(setCookie.contains("SameSite=Lax"));
    }

    @Test
    void shouldClearH5SessionCookieWithSamePolicy() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        H5SessionCookie.clear(response);

        String setCookie = response.getHeader("Set-Cookie");
        assertTrue(setCookie != null && setCookie.contains("H5_SESSION="));
        assertTrue(setCookie.contains("Max-Age=0"));
        assertTrue(setCookie.contains("Path=/"));
    }
}
