package com.cloud.userauth.infrastructure.oauth2.sas.logout;

import static org.junit.jupiter.api.Assertions.assertFalse;

import com.cloud.userauth.domain.authentication.session.SessionId;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.cloud.userauth.infrastructure.oauth2.sas.logout.SasSessionAuthorizationRevoker;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

class SasSessionAuthorizationRevokerTest {
    @Test
    void shouldInvalidateAccessAndRefreshTokensForSessionAuthorization() {
        InMemoryAuthorizations authorizations = new InMemoryAuthorizations();
        SessionId sessionId = new SessionId("session-1");
        authorizations.save(authorization(sessionId.value()));

        new SasSessionAuthorizationRevoker(authorizations).revoke(sessionId);

        OAuth2Authorization authorization = authorizations.findById(sessionId.value());
        assertFalse(authorization.getAccessToken().isActive());
        assertFalse(authorization.getRefreshToken().isActive());
    }

    private static OAuth2Authorization authorization(String sessionId) {
        Instant now = Instant.parse("2099-08-02T00:00:00Z");
        RegisteredClient client = RegisteredClient.withId("client-record-id")
                .clientId("client-id")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .scope("user.read")
                .build();
        return OAuth2Authorization.withRegisteredClient(client)
                .id(sessionId)
                .principalName("1001")
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .authorizedScopes(Set.of("user.read"))
                .accessToken(new OAuth2AccessToken(
                        OAuth2AccessToken.TokenType.BEARER, "access-token", now, now.plusSeconds(300)))
                .refreshToken(new OAuth2RefreshToken("refresh-token", now, now.plusSeconds(600)))
                .build();
    }

    private static final class InMemoryAuthorizations implements OAuth2AuthorizationService {
        private final Map<String, OAuth2Authorization> values = new HashMap<>();

        @Override public void save(OAuth2Authorization authorization) { values.put(authorization.getId(), authorization); }
        @Override public void remove(OAuth2Authorization authorization) { values.remove(authorization.getId()); }
        @Override public OAuth2Authorization findById(String id) { return values.get(id); }
        @Override
        public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
            return values.values().stream()
                    .filter(authorization -> authorization.getAccessToken() != null
                            && authorization.getAccessToken().getToken().getTokenValue().equals(token))
                    .findFirst()
                    .orElse(null);
        }
    }
}
