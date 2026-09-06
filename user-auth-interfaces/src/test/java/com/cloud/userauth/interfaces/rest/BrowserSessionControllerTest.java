package com.cloud.userauth.interfaces.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cloud.framework.core.AuthenticatedSessionClientRequest;
import com.cloud.framework.core.Result;
import com.cloud.userauth.api.authentication.EndBrowserSessionApiCommand;
import com.cloud.userauth.api.authentication.VerifyBrowserSessionApiCommand;
import com.cloud.userauth.api.authentication.VerifyBrowserSessionApiCommandOutput;
import com.cloud.userauth.api.facade.BrowserSessionCommandFacade;
import com.cloud.userauth.application.common.ApplicationException;
import jakarta.servlet.http.Cookie;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class BrowserSessionControllerTest {

    @Test
    void shouldEndSessionWithCookieCredential() {
        AtomicReference<EndBrowserSessionApiCommand> captured = new AtomicReference<>();
        BrowserSessionController controller = controller(captured);
        MockHttpServletResponse response = new MockHttpServletResponse();

        Result<Void> result = controller.end(request(), servletRequest("credential-1"), response);

        assertEquals(Result.success(), result);
        assertEquals(new EndBrowserSessionApiCommand(1001L, "session-1", "credential-1"),
                captured.get());
        assertTrue(response.getHeader("Set-Cookie").contains("BROWSER_SESSION="));
        assertTrue(response.getHeader("Set-Cookie").contains("Max-Age=0"));
    }

    @Test
    void shouldRejectRequestWithoutCookie() {
        BrowserSessionController controller = controller(new AtomicReference<>());

        assertThrows(ApplicationException.class,
                () -> controller.end(request(), servletRequest(null), new MockHttpServletResponse()));
    }

    @Test
    private static BrowserSessionController controller(
            AtomicReference<EndBrowserSessionApiCommand> captured
    ) {
        BrowserSessionCommandFacade facade = new BrowserSessionCommandFacade() {
            @Override
            public Result<VerifyBrowserSessionApiCommandOutput> verify(
                    VerifyBrowserSessionApiCommand command
            ) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Result<Void> end(EndBrowserSessionApiCommand command) {
                captured.set(command);
                return Result.success();
            }
        };
        return new BrowserSessionController(facade);
    }

    private static AuthenticatedSessionClientRequest request() {
        AuthenticatedSessionClientRequest request = new AuthenticatedSessionClientRequest();
        request.setUserId("1001");
        request.setSessionId("session-1");
        return request;
    }

    private static MockHttpServletRequest servletRequest(String credential) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        if (credential != null) {
            request.setCookies(new Cookie("BROWSER_SESSION", credential));
        }
        return request;
    }
}
