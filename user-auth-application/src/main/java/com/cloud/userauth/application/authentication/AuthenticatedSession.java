package com.cloud.userauth.application.authentication;

import com.cloud.userauth.domain.authentication.account.AuthAccountId;
import com.cloud.userauth.domain.authentication.session.SessionId;
import com.cloud.userauth.domain.user.UserId;

/** 与认证交付协议无关的已认证登录会话。 */
public record AuthenticatedSession(UserId userId, AuthAccountId authAccountId, SessionId sessionId) {
}
