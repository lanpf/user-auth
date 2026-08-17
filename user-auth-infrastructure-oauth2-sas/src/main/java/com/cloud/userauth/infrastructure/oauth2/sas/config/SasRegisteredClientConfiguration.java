package com.cloud.userauth.infrastructure.oauth2.sas.config;

import com.cloud.userauth.infrastructure.oauth2.sas.grant.mobileotp.MobileOtpGrantTypes;
import com.cloud.userauth.infrastructure.oauth2.sas.grant.external.ExternalIdentityGrantTypes;
import com.cloud.userauth.infrastructure.config.AccessTokenProperties;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(
        prefix = "user-auth.authentication.oauth2.authorization-server",
        name = "enabled",
        havingValue = "true")
public class SasRegisteredClientConfiguration {

    @Bean
    public RegisteredClientRepository registeredClientRepository(JdbcOperations jdbcOperations) {
        return new JdbcRegisteredClientRepository(jdbcOperations);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public InitializingBean bootstrapRegisteredClient(
            RegisteredClientRepository repository,
            PasswordEncoder passwordEncoder,
            SasAuthorizationServerProperties properties,
            AccessTokenProperties accessTokenProperties
    ) {
        return () -> {
            SasAuthorizationServerProperties.InternalTokenClientProperties internalTokenClient =
                    properties.getInternalTokenClient();
            String clientId = internalTokenClient.getClientId();
            String clientSecret = internalTokenClient.getClientSecret();
            RegisteredClient existing = repository.findByClientId(clientId);
            RegisteredClient.Builder builder = RegisteredClient
                    .withId(existing == null ? stableClientId(clientId) : existing.getId())
                    .clientId(clientId)
                    .clientIdIssuedAt(existing == null ? null : existing.getClientIdIssuedAt())
                    .clientSecret(encodedSecret(existing, clientSecret, passwordEncoder))
                    .clientName("user-auth internal token client")
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .authorizationGrantType(MobileOtpGrantTypes.MOBILE_OTP)
                    .authorizationGrantType(ExternalIdentityGrantTypes.EXTERNAL_IDENTITY)
                    .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                    .clientSettings(ClientSettings.builder()
                            .requireAuthorizationConsent(false)
                            .build())
                    .tokenSettings(TokenSettings.builder()
                            .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED)
                            .accessTokenTimeToLive(accessTokenProperties.getTtl())
                            .refreshTokenTimeToLive(properties.getRefreshToken().getTtl())
                            .reuseRefreshTokens(false)
                            .build());
            properties.getScopes().forEach(builder::scope);
            repository.save(builder.build());
        };
    }

    private static String encodedSecret(
            RegisteredClient existing,
            String configuredSecret,
            PasswordEncoder passwordEncoder
    ) {
        if (existing != null
                && existing.getClientSecret() != null
                && passwordEncoder.matches(configuredSecret, existing.getClientSecret())) {
            return existing.getClientSecret();
        }
        return passwordEncoder.encode(configuredSecret);
    }

    private static String stableClientId(String clientId) {
        return UUID.nameUUIDFromBytes(clientId.getBytes(StandardCharsets.UTF_8)).toString();
    }
}
