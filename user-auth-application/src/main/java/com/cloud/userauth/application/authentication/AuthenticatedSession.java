package com.cloud.userauth.application.authentication;

import com.cloud.userauth.domain.authentication.session.SessionId;

/** 与认证交付协议无关的已认证登录会话。 */
public record AuthenticatedSession(Long userId, Long authAccountId, SessionId sessionId) {
}
