package com.cloud.userauth.application.port;

import com.cloud.userauth.domain.authentication.session.SessionId;

/** 撤销某个登录会话关联的协议授权和 Token。 */
public interface SessionAuthorizationRevoker extends LoginSessionArtifactRevoker {
    void revoke(SessionId sessionId);

    @Override
    default void revokeByLoginSessionId(SessionId loginSessionId) {
        revoke(loginSessionId);
    }
}
