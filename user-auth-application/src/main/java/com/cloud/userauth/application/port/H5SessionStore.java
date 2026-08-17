package com.cloud.userauth.application.port;

import com.cloud.userauth.application.authentication.AuthenticatedSession;
import com.cloud.userauth.domain.authentication.session.SessionId;
import java.time.Duration;
import java.util.Optional;

/** H5 Cookie 会话的创建、解析、滑动续期与级联撤销端口。 */
public interface H5SessionStore extends LoginSessionArtifactRevoker {
    H5Session create(
            AuthenticatedSession authenticatedSession,
            Duration idleTtl,
            Duration absoluteTtl
    );

    Optional<ResolvedH5Session> find(String credential);

    record H5Session(String credential, Duration idleTtl) {
    }

    record ResolvedH5Session(
            AuthenticatedSession authenticatedSession,
            Duration remainingIdleTtl,
            boolean renewed
    ) {
    }
}
