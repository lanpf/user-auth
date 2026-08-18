package com.cloud.userauth.infrastructure.oauth2.sas.logout;

import static org.junit.jupiter.api.Assertions.assertFalse;

import com.cloud.userauth.domain.authentication.session.SessionId;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

class SasLoginSessionRevokerTest {
    @Test
    void shouldInvalidateAuthorizationTokensForLoginSession() {
        Authorizations authorizations = new Authorizations(authorization());
        SessionId sessionId = new SessionId("session-1");

        new SasLoginSessionRevoker(authorizations).revoke(sessionId);

        assertFalse(authorizations.authorization.getAccessToken().isActive());
        assertFalse(authorizations.authorization.getRefreshToken().isActive());
    }

    private static OAuth2Authorization authorization() {
        RegisteredClient client = RegisteredClient.withId("client-id")
                .clientId("client")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .build();
        Instant now = Instant.now();
        return OAuth2Authorization.withRegisteredClient(client)
                .id("session-1")
                .principalName("1001")
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .accessToken(new OAuth2AccessToken(
                        OAuth2AccessToken.TokenType.BEARER,
                        "access-token",
                        now,
                        now.plusSeconds(300),
                        Set.of()))
                .refreshToken(new OAuth2RefreshToken(
                        "refresh-token",
                        now,
                        now.plusSeconds(600)))
                .build();
    }

    private static final class Authorizations implements OAuth2AuthorizationService {
        private OAuth2Authorization authorization;

        private Authorizations(OAuth2Authorization authorization) {
            this.authorization = authorization;
        }

        @Override
        public void save(OAuth2Authorization value) {
            authorization = value;
        }

        @Override
        public void remove(OAuth2Authorization value) {
            authorization = null;
        }

        @Override
        public OAuth2Authorization findById(String id) {
            return authorization != null && authorization.getId().equals(id)
                    ? authorization
                    : null;
        }

        @Override
        public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
            return null;
        }
    }
}
