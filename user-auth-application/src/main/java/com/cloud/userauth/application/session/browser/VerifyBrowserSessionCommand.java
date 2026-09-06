package com.cloud.userauth.application.session.browser;

/** 验证浏览器会话的命令；credential 为该会话的凭据。 */
public record VerifyBrowserSessionCommand(
        String credential
) {
}
