package com.cloud.userauth.infrastructure.oauth2.redis;

import java.util.Optional;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;

public interface OAuth2AuthorizationRedisStore {
    void save(OAuth2Authorization authorization);

    void remove(OAuth2Authorization authorization);

    OAuth2Authorization findById(String authorizationId);

    OAuth2Authorization findByTokenHash(String tokenHash, OAuth2TokenType tokenType);

    Optional<String> findReusedRefreshTokenAuthorizationId(String refreshTokenHash);
}
