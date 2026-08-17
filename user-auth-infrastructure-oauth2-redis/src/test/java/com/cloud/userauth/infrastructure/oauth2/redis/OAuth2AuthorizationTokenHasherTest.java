package com.cloud.userauth.infrastructure.oauth2.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.time.Instant;
import java.util.Set;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

class OAuth2AuthorizationTokenHasherTest {
    private final OAuth2AuthorizationTokenHasher tokenHasher =
            new OAuth2AuthorizationTokenHasher();

    @Test
    void shouldReplaceAccessAndRefreshTokenValuesWithHashes() {
        OAuth2Authorization authorization =
                authorization("raw-access-token", "raw-refresh-token");

        OAuth2Authorization stored = tokenHasher.sanitize(authorization);

        assertEquals(
                DigestUtils.sha256Hex("raw-access-token"),
                stored.getAccessToken().getToken().getTokenValue());
        assertEquals(
                DigestUtils.sha256Hex("raw-refresh-token"),
                stored.getRefreshToken().getToken().getTokenValue());
        assertNotEquals(
                authorization.getRefreshToken().getToken().getTokenValue(),
                stored.getRefreshToken().getToken().getTokenValue());
    }

    @Test
    void shouldNotHashAlreadyHashedTokensAgain() {
        OAuth2Authorization stored = tokenHasher.sanitize(
                authorization("raw-access-token", "raw-refresh-token"));

        OAuth2Authorization restoredAndSaved = tokenHasher.sanitize(stored);

        assertEquals(
                stored.getAccessToken().getToken().getTokenValue(),
                restoredAndSaved.getAccessToken().getToken().getTokenValue());
        assertEquals(
                stored.getRefreshToken().getToken().getTokenValue(),
                restoredAndSaved.getRefreshToken().getToken().getTokenValue());
    }

    private OAuth2Authorization authorization(
            String accessTokenValue,
            String refreshTokenValue
    ) {
        Instant now = Instant.parse("2026-07-30T00:00:00Z");
        RegisteredClient client = registeredClient();
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                accessTokenValue,
                now,
                now.plusSeconds(300),
                Set.of("user.read"));
        OAuth2RefreshToken refreshToken =
                new OAuth2RefreshToken(refreshTokenValue, now, now.plusSeconds(600));
        return OAuth2Authorization.withRegisteredClient(client)
                .id("session-1")
                .principalName("1001")
                .authorizationGrantType(client.getAuthorizationGrantTypes().iterator().next())
                .authorizedScopes(Set.of("user.read"))
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
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
}
