package com.cloud.userauth.application.session.browser;

import com.cloud.userauth.domain.authentication.session.SessionId;
import com.cloud.userauth.domain.user.UserId;

/** 结束单个浏览器会话的命令；credential 为该会话的凭据。 */
public record EndBrowserSessionCommand(
        UserId userId,
        SessionId sessionId,
        String credential
) {
}
