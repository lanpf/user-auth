package com.cloud.userauth.application.port;

import com.cloud.userauth.application.authentication.AuthenticatedSession;
import java.time.Duration;
import java.util.Optional;

/** 不透明 Session Token 的外层存储端口。 */
public interface SessionTokenStore extends LoginSessionArtifactRevoker {
    IssuedSessionToken create(AuthenticatedSession authenticatedSession, Duration ttl);

    Optional<AuthenticatedSession> findAuthenticatedSession(String token);

    record IssuedSessionToken(String token, Duration ttl) {
    }
}
