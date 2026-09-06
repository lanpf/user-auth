package com.cloud.userauth.application.session.browser;

import com.cloud.userauth.application.port.BrowserSessionStore;
import lombok.RequiredArgsConstructor;

/** 浏览器会话的在线验证用例：校验会话状态并按需滑动续期，供入口层逐请求调用。 */
@RequiredArgsConstructor
public class VerifyBrowserSessionCommandService {
    private final BrowserSessionStore browserSessionStore;

    public VerifyBrowserSessionOutput execute(VerifyBrowserSessionCommand command) {
        return browserSessionStore.find(command.credential())
                .<VerifyBrowserSessionOutput>map(resolved ->
                        new VerifyBrowserSessionOutput.Verified(
                                resolved.authenticatedSession().userId(),
                                resolved.authenticatedSession().sessionId(),
                                resolved.remainingIdleTtl()))
                .orElseGet(VerifyBrowserSessionOutput.Unverified::new);
    }
}
