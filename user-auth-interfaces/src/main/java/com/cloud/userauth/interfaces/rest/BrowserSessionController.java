package com.cloud.userauth.interfaces.rest;

import com.cloud.framework.core.AuthenticatedSessionClientRequest;
import com.cloud.framework.core.Result;
import com.cloud.userauth.api.authentication.EndBrowserSessionApiCommand;
import com.cloud.userauth.api.constants.UserAuthPathApiConstants;
import com.cloud.userauth.api.facade.BrowserSessionCommandFacade;
import com.cloud.userauth.application.common.ApplicationError;
import com.cloud.userauth.application.common.ApplicationException;
import com.cloud.userauth.interfaces.security.BrowserSessionCookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 浏览器载体使用 BROWSER_SESSION Cookie 结束当前浏览器会话；不能复用宿主 logout。
 */
@RestController
@RequiredArgsConstructor
public class BrowserSessionController {
    private final BrowserSessionCommandFacade facade;

    @PostMapping(UserAuthPathApiConstants.API_BROWSER_SESSIONS_END)
    public Result<Void> end(
            @Valid AuthenticatedSessionClientRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse
    ) {
        String credential = BrowserSessionCookie.read(servletRequest)
                .orElseThrow(() -> new ApplicationException(ApplicationError.APP_BROWSER_SESSION_INVALID));
        Result<Void> result = facade.end(new EndBrowserSessionApiCommand(
                Long.valueOf(request.getUserId()),
                request.getSessionId(),
                credential));
        BrowserSessionCookie.clear(servletResponse);
        return result;
    }
}
