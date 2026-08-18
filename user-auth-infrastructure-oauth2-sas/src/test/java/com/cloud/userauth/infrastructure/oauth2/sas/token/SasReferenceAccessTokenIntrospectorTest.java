package com.cloud.userauth.infrastructure.oauth2.sas.token;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cloud.userauth.api.constants.AccessTokenClaimApiConstants;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException;

class SasReferenceAccessTokenIntrospectorTest {
    private static final Instant NOW = Instant.now();

    @Test
    void shouldRestoreSessionIdentityAndScopeAuthorities() {
        SasReferenceAccessTokenIntrospector introspector =
                new SasReferenceAccessTokenIntrospector(new AuthorizationService(authorization(false)));

        var principal = introspector.introspect("reference-token");

        assertEquals("1001", principal.getName());
        assertEquals(Long.valueOf(1001L), principal.<Long>getAttribute(
                AccessTokenClaimApiConstants.USER_ID_CLAIM));
        assertEquals(Set.of("SCOPE_app"), principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(java.util.stream.Collectors.toSet()));
    }

    @Test
    void shouldRejectInvalidatedReferenceAccessToken() {
        SasReferenceAccessTokenIntrospector introspector =
                new SasReferenceAccessTokenIntrospector(new AuthorizationService(authorization(true)));

        assertThrows(BadOpaqueTokenException.class,
                () -> introspector.introspect("reference-token"));
    }

    private static OAuth2Authorization authorization(boolean invalidated) {
        RegisteredClient client = RegisteredClient.withId("client-record-id")
                .clientId("client-id")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("app")
                .build();
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "reference-token",
                NOW,
                NOW.plusSeconds(300),
                Set.of("app"));
        OAuth2Authorization.Builder builder = OAuth2Authorization.withRegisteredClient(client)
                .id("session-1")
                .principalName("1001")
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .authorizedScopes(Set.of("app"))
                .token(accessToken, metadata -> metadata.put(
                        OAuth2Authorization.Token.CLAIMS_METADATA_NAME,
                        java.util.Map.of(
                                AccessTokenClaimApiConstants.USER_ID_CLAIM, 1001L,
                                AccessTokenClaimApiConstants.AUTH_ACCOUNT_ID_CLAIM, 2001L,
                                AccessTokenClaimApiConstants.SESSION_ID_CLAIM, "session-1")));
        if (invalidated) {
            builder.invalidate(accessToken);
        }
        return builder.build();
    }

    private record AuthorizationService(
            OAuth2Authorization authorization
    ) implements OAuth2AuthorizationService {
        @Override
        public void save(OAuth2Authorization value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void remove(OAuth2Authorization value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public OAuth2Authorization findById(String id) {
            return authorization.getId().equals(id) ? authorization : null;
        }

        @Override
        public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
            return "reference-token".equals(token)
                    && OAuth2TokenType.ACCESS_TOKEN.equals(tokenType)
                    ? authorization
                    : null;
        }
    }
}
