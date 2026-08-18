package com.cloud.userauth.application.port;

import com.cloud.userauth.domain.authentication.session.SessionId;

/** 撤销一个 LoginSession 派生的宿主凭据、协议授权或子会话。 */
@FunctionalInterface
public interface LoginSessionRevoker {
    void revoke(SessionId loginSessionId);
}
