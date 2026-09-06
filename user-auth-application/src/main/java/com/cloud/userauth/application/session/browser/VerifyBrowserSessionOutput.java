package com.cloud.userauth.application.session.browser;

import com.cloud.userauth.domain.authentication.session.SessionId;
import com.cloud.userauth.domain.user.UserId;
import java.time.Duration;

/** 浏览器会话在线验证输出，明确区分验证通过与凭据无效。 */
public sealed interface VerifyBrowserSessionOutput {

    record Verified(
            UserId userId,
            SessionId sessionId,
            Duration remainingIdleTtl
    ) implements VerifyBrowserSessionOutput {
    }

    record Unverified() implements VerifyBrowserSessionOutput {
    }
}
