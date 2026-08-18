package com.cloud.userauth.application.port;

import com.cloud.userauth.application.authentication.AuthenticatedSession;
import java.time.Duration;
import java.util.Optional;

/** 浏览器会话创建、解析、滑动续期与级联撤销端口。 */
public interface BrowserSessionStore extends LoginSessionRevoker {
    CreatedSession create(
            AuthenticatedSession authenticatedSession,
            Duration idleTtl,
            Duration absoluteTtl
    );

    Optional<ResolvedSession> find(String credential);

    record CreatedSession(String credential, Duration idleTtl) {
    }

    record ResolvedSession(
            AuthenticatedSession authenticatedSession,
            Duration remainingIdleTtl,
            boolean renewed
    ) {
    }
}
