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

    /** 精确结束单个 Browser Session（不影响同一父 LoginSession 的其他 Browser Session）。 */
    boolean end(String credential);

    record CreatedSession(String credential, Duration absoluteTtl) {
    }

    record ResolvedSession(
            AuthenticatedSession authenticatedSession,
            Duration remainingIdleTtl
    ) {
    }
}
