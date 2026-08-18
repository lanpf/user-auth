package com.cloud.userauth.infrastructure.oauth2.sas.config;

import com.cloud.userauth.api.constants.AccessTokenClaimApiConstants;
import com.cloud.userauth.infrastructure.oauth2.sas.protocol.SasAuthorizationAttributes;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SasTokenConfigurationTest {
    private static final OAuth2TokenType ID_TOKEN =
            new OAuth2TokenType("id_token");

    @Test
    void shouldAddConfiguredAudienceToAccessToken() {
        SasAuthorizationServerProperties properties = properties();
        JwtClaimsSet claims = customize(properties, OAuth2TokenType.ACCESS_TOKEN);

        assertEquals(
                properties.getAudiences(),
                Set.copyOf(claims.getAudience()));
        assertEquals("100001", claims.getSubject());
        assertNull(claims.getClaim("user_id"));
        assertEquals(Long.valueOf(1001L), claims.getClaim(AccessTokenClaimApiConstants.AUTH_ACCOUNT_ID_CLAIM));
        assertEquals("session-1", claims.getClaim(AccessTokenClaimApiConstants.SESSION_ID_CLAIM));
    }

    @Test
    void shouldNotOverrideAudienceForOtherJwtTypes() {
        JwtClaimsSet claims = customize(properties(), ID_TOKEN);

        assertNull(claims.getAudience());
        assertNull(claims.getClaim("user_id"));
        assertNull(claims.getClaim(AccessTokenClaimApiConstants.AUTH_ACCOUNT_ID_CLAIM));
        assertNull(claims.getClaim(AccessTokenClaimApiConstants.SESSION_ID_CLAIM));
    }

    @Test
    void shouldValidateJwtSignedByAuthorizationServerKey() throws Exception {
        SasAuthorizationServerProperties properties = properties();
        JWKSource<SecurityContext> jwkSource =
                new SasTokenConfiguration.NonProductionJwkConfiguration().jwkSource();
        JwtEncoder encoder = new NimbusJwtEncoder(jwkSource);
        Instant now = Instant.now();
        String encoded = encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(SignatureAlgorithm.RS256).build(),
                JwtClaimsSet.builder()
                        .issuer(properties.getIssuer())
                        .subject("100001")
                        .issuedAt(now)
                        .expiresAt(now.plusSeconds(300))
                        .build())).getTokenValue();

        Jwt decoded = new SasTokenConfiguration().jwtDecoder(jwkSource, properties).decode(encoded);

        assertEquals("100001", decoded.getSubject());
    }

    private static JwtClaimsSet customize(
            SasAuthorizationServerProperties properties,
            OAuth2TokenType tokenType
    ) {
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder();
        claims.issuer(properties.getIssuer());
        JwtEncodingContext context = JwtEncodingContext
                .with(
                        JwsHeader.with(SignatureAlgorithm.RS256),
                        claims)
                .authorization(authorization())
                .tokenType(tokenType)
                .build();
        OAuth2TokenCustomizer<JwtEncodingContext> customizer =
                new SasTokenConfiguration().jwtCustomizer(properties);
        customizer.customize(context);
        return claims.build();
    }

    private static SasAuthorizationServerProperties properties() {
        SasAuthorizationServerProperties properties =
                SasAuthorizationServerPropertiesFixtures.defaults();
        properties.getAudiences().add("cloud-platform-api");
        return properties;
    }

    private static OAuth2Authorization authorization() {
        RegisteredClient registeredClient = RegisteredClient
                .withId("registered-client-id")
                .clientId("internal-client")
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .build();
        return OAuth2Authorization
                .withRegisteredClient(registeredClient)
                .principalName("100001")
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .attribute(SasAuthorizationAttributes.USER_ID, 100001L)
                .attribute(SasAuthorizationAttributes.AUTH_ACCOUNT_ID, 1001L)
                .attribute(SasAuthorizationAttributes.SESSION_ID, "session-1")
                .build();
    }
}
