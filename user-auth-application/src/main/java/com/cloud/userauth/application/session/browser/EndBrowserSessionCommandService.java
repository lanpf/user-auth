package com.cloud.userauth.application.session.browser;

import com.cloud.userauth.application.common.ApplicationError;
import com.cloud.userauth.application.common.ApplicationException;
import com.cloud.userauth.application.port.BrowserSessionStore;
import lombok.RequiredArgsConstructor;

/** 浏览器载体自行结束当前 Browser Session 的用例；按凭据精确撤销，不影响同父 LoginSession 的其他浏览器会话。 */
@RequiredArgsConstructor
public class EndBrowserSessionCommandService {
    private final BrowserSessionStore browserSessionStore;

    public void execute(EndBrowserSessionCommand command) {
        browserSessionStore.find(command.credential())
                .filter(resolved -> resolved.authenticatedSession().userId().equals(command.userId()))
                .filter(resolved -> resolved.authenticatedSession().sessionId().equals(command.sessionId()))
                .orElseThrow(() -> new ApplicationException(ApplicationError.APP_BROWSER_SESSION_INVALID));
        browserSessionStore.end(command.credential());
    }
}
