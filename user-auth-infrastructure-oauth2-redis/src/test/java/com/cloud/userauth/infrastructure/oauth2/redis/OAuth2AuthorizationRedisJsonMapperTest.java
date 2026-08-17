package com.cloud.userauth.infrastructure.oauth2.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

class OAuth2AuthorizationRedisJsonMapperTest {
    @Test
    void shouldRestoreAuthorizationFromRedisJson() {
        RegisteredClient registeredClient = registeredClient();
        OAuth2AuthorizationRedisJsonMapper mapper =
                new OAuth2AuthorizationRedisJsonMapper(
                        new SingleRegisteredClientRepository(registeredClient));
        OAuth2Authorization authorization = authorization(registeredClient);

        OAuth2Authorization restored = mapper.read(mapper.write(authorization));

        assertEquals(authorization.getPrincipalName(), restored.getPrincipalName());
        assertEquals("access-token", restored.getAccessToken().getToken().getTokenValue());
        assertEquals("refresh-token", restored.getRefreshToken().getToken().getTokenValue());
        assertEquals(1001L, ((Number) restored.getAttribute("user_id")).longValue());
        assertEquals(
                "metadata-value",
                restored.getAccessToken().getMetadata().get("metadata-key"));
        assertNotNull(restored.getAttribute(Principal.class.getName()));
    }

    private OAuth2Authorization authorization(RegisteredClient registeredClient) {
        Instant now = Instant.parse("2026-07-30T00:00:00Z");
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "access-token",
                now,
                now.plusSeconds(300),
                Set.of("user.read"));
        OAuth2RefreshToken refreshToken =
                new OAuth2RefreshToken("refresh-token", now, now.plusSeconds(600));
        OAuth2Authorization.Builder builder = OAuth2Authorization
                .withRegisteredClient(registeredClient)
                .id("session-1")
                .principalName("1001")
                .authorizationGrantType(registeredClient
                        .getAuthorizationGrantTypes()
                        .iterator()
                        .next())
                .authorizedScopes(Set.of("user.read"))
                .attribute(
                        Principal.class.getName(),
                        UsernamePasswordAuthenticationToken.authenticated(
                                "1001",
                                null,
                                List.of()))
                .attribute("user_id", 1001L)
                .refreshToken(refreshToken);
        builder.token(accessToken, metadata ->
                metadata.put("metadata-key", "metadata-value"));
        return builder.build();
    }

    private RegisteredClient registeredClient() {
        return RegisteredClient.withId("client-record-id")
                .clientId("client-id")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(new AuthorizationGrantType(
                        "urn:ietf:params:oauth:grant-type:mobile_otp"))
                .scope("user.read")
                .build();
    }

    private record SingleRegisteredClientRepository(RegisteredClient registeredClient)
            implements RegisteredClientRepository {
        @Override
        public void save(RegisteredClient registeredClient) {
            throw new UnsupportedOperationException();
        }

        @Override
        public RegisteredClient findById(String id) {
            return registeredClient.getId().equals(id) ? registeredClient : null;
        }

        @Override
        public RegisteredClient findByClientId(String clientId) {
            return registeredClient.getClientId().equals(clientId) ? registeredClient : null;
        }
    }
}
