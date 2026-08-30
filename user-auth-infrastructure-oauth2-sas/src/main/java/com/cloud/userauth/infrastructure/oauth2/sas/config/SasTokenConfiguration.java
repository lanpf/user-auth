package com.cloud.userauth.infrastructure.oauth2.sas.config;

import com.cloud.userauth.api.constants.AccessTokenClaimApiConstants;
import com.cloud.userauth.infrastructure.config.AccessTokenProperties;
import com.cloud.userauth.infrastructure.oauth2.sas.protocol.SasAuthorizationAttributes;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.DelegatingOAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.JwtGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2AccessTokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenClaimsContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2RefreshTokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.UUID;

@Configuration(proxyBeanMethods = false)
@Import({
        SasTokenConfiguration.NonProductionJwkConfiguration.class,
        SasTokenConfiguration.ProductionJwkConfiguration.class
})
public class SasTokenConfiguration {
    private static final String RSA_KEY_PAIR_REQUIRED = "OAuth2 signing key must be an RSA key pair";
    @Bean
    public AuthorizationServerSettings authorizationServerSettings(
            SasAuthorizationServerProperties properties
    ) {
        return AuthorizationServerSettings.builder()
                .issuer(properties.getIssuer())
                .build();
    }

    @Bean
    public OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator(
            JWKSource<SecurityContext> jwkSource,
            OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer,
            OAuth2TokenCustomizer<OAuth2TokenClaimsContext> referenceAccessTokenCustomizer
    ) {
        JwtEncoder jwtEncoder = new NimbusJwtEncoder(jwkSource);
        JwtGenerator jwtGenerator = new JwtGenerator(jwtEncoder);
        jwtGenerator.setJwtCustomizer(jwtCustomizer);
        OAuth2AccessTokenGenerator accessTokenGenerator = new OAuth2AccessTokenGenerator();
        accessTokenGenerator.setAccessTokenCustomizer(referenceAccessTokenCustomizer);
        return new DelegatingOAuth2TokenGenerator(
                jwtGenerator,
                accessTokenGenerator,
                new OAuth2RefreshTokenGenerator());
    }

    @Bean
    @ConditionalOnProperty(
            prefix = AccessTokenProperties.PREFIX,
            name = "format",
            havingValue = "SELF_CONTAINED",
            matchIfMissing = true)
    public JwtDecoder jwtDecoder(
            JWKSource<SecurityContext> jwkSource,
            SasAuthorizationServerProperties properties
    ) {
        DefaultJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
        jwtProcessor.setJWSKeySelector(
                new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, jwkSource));
        NimbusJwtDecoder decoder = new NimbusJwtDecoder(jwtProcessor);
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(properties.getIssuer()));
        return decoder;
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer(
            SasAuthorizationServerProperties properties
    ) {
        return context -> {
            OAuth2Authorization authorization = context.getAuthorization();
            if (authorization == null) {
                return;
            }
            if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                context.getClaims()
                        .audience(List.copyOf(properties.getAudiences()))
                        .subject(authorization.getPrincipalName())
                        .claim(
                                AccessTokenClaimApiConstants.AUTH_ACCOUNT_ID_CLAIM,
                                authorization.getAttribute(SasAuthorizationAttributes.AUTH_ACCOUNT_ID))
                        .claim(
                                AccessTokenClaimApiConstants.SESSION_ID_CLAIM,
                                authorization.getAttribute(SasAuthorizationAttributes.SESSION_ID));
            }
        };
    }

    @Bean
    public OAuth2TokenCustomizer<OAuth2TokenClaimsContext> referenceAccessTokenCustomizer(
            SasAuthorizationServerProperties properties
    ) {
        return context -> {
            OAuth2Authorization authorization = context.getAuthorization();
            if (authorization == null) {
                return;
            }
            context.getClaims()
                    .audience(List.copyOf(properties.getAudiences()))
                    .subject(authorization.getPrincipalName())
                    .claim(
                            AccessTokenClaimApiConstants.AUTH_ACCOUNT_ID_CLAIM,
                            authorization.getAttribute(SasAuthorizationAttributes.AUTH_ACCOUNT_ID))
                    .claim(
                            AccessTokenClaimApiConstants.SESSION_ID_CLAIM,
                            authorization.getAttribute(SasAuthorizationAttributes.SESSION_ID));
        };
    }

    @Configuration(proxyBeanMethods = false)
    @Profile("!prod")
    static class NonProductionJwkConfiguration {
        @Bean
        JWKSource<SecurityContext> jwkSource() throws Exception {
            RSAKey rsaKey = new RSAKeyGenerator(2048)
                    .keyID(UUID.randomUUID().toString())
                    .generate();
            JWKSet jwkSet = new JWKSet(rsaKey);
            return (selector, context) -> selector.select(jwkSet);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @Profile("prod")
    @EnableConfigurationProperties(SasAuthorizationServerProperties.SignatureProperties.class)
    static class ProductionJwkConfiguration {
        @Bean
        JWKSource<SecurityContext> jwkSource(
                SasAuthorizationServerProperties.SignatureProperties properties,
                ResourceLoader resourceLoader
        ) throws Exception {
            SasAuthorizationServerProperties.KeyStoreProperties keyStoreProperties = properties.getKeyStore();
            Resource resource = resourceLoader.getResource(keyStoreProperties.getLocation());
            KeyStore keyStore = KeyStore.getInstance(keyStoreProperties.getType());
            try (InputStream input = resource.getInputStream()) {
                keyStore.load(input, keyStoreProperties.getPassword().toCharArray());
            }
            char[] keyPassword = properties.getKeyPassword().toCharArray();
            PrivateKey privateKey =
                    (PrivateKey) keyStore.getKey(properties.getKeyAlias(), keyPassword);
            Certificate certificate = keyStore.getCertificate(properties.getKeyAlias());
            Assert.state(certificate != null, RSA_KEY_PAIR_REQUIRED);
            Assert.state(privateKey instanceof RSAPrivateKey, RSA_KEY_PAIR_REQUIRED);
            Assert.state(certificate.getPublicKey() instanceof RSAPublicKey, RSA_KEY_PAIR_REQUIRED);
            RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) privateKey;
            RSAPublicKey rsaPublicKey = (RSAPublicKey) certificate.getPublicKey();
            RSAKey rsaKey = new RSAKey.Builder(rsaPublicKey)
                    .privateKey(rsaPrivateKey)
                    .keyID(properties.getKeyId())
                    .build();
            JWKSet jwkSet = new JWKSet(rsaKey);
            return (selector, context) -> selector.select(jwkSet);
        }
    }
}
