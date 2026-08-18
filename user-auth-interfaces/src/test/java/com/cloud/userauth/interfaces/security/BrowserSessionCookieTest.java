package com.cloud.userauth.interfaces.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.servlet.http.Cookie;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class BrowserSessionCookieTest {
    @Test
    void shouldReadBrowserSessionCookie() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(
                new Cookie("OTHER", "other"),
                new Cookie(BrowserSessionCookie.COOKIE_NAME, "browser-token"));

        assertEquals("browser-token", BrowserSessionCookie.read(request).orElseThrow());
    }

    @Test
    void shouldWriteSecureHttpOnlyBrowserSessionCookie() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        BrowserSessionCookie.write(response, "browser-token", Duration.ofMinutes(30));

        String setCookie = response.getHeader("Set-Cookie");
        assertTrue(setCookie != null && setCookie.contains("BROWSER_SESSION=browser-token"));
        assertTrue(setCookie.contains("Max-Age=1800"));
        assertTrue(setCookie.contains("Secure"));
        assertTrue(setCookie.contains("HttpOnly"));
        assertTrue(setCookie.contains("SameSite=Lax"));
    }

    @Test
    void shouldClearBrowserSessionCookieWithSamePolicy() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        BrowserSessionCookie.clear(response);

        String setCookie = response.getHeader("Set-Cookie");
        assertTrue(setCookie != null && setCookie.contains("BROWSER_SESSION="));
        assertTrue(setCookie.contains("Max-Age=0"));
        assertTrue(setCookie.contains("Path=/"));
    }
}
