package com.cloud.userauth.infrastructure.oauth2.sas.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cloud.userauth.infrastructure.oauth2.sas.config.SasAuthorizationServerProperties;
import com.cloud.userauth.infrastructure.oauth2.sas.config.SasRegisteredClientConfiguration;
import com.cloud.userauth.infrastructure.oauth2.sas.grant.external.ExternalIdentityGrantTypes;
import com.cloud.userauth.infrastructure.oauth2.sas.grant.mobileotp.MobileOtpGrantTypes;
import com.cloud.userauth.infrastructure.config.AccessTokenProperties;
import java.time.Duration;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

class SasRegisteredClientConfigurationTest {

    @Test
    void shouldRegisterConfidentialClientUsingEncodedSecretAndHttpBasic() throws Exception {
        SasRegisteredClientConfiguration configuration =
                new SasRegisteredClientConfiguration();
        PasswordEncoder passwordEncoder = configuration.passwordEncoder();
        String clientSecret = "server-side-secret";
        SasAuthorizationServerProperties properties =
                SasAuthorizationServerPropertiesFixtures.withInternalClient(
                        "user-auth-internal-test",
                        clientSecret);
        properties.getScopes().addAll(Set.of("app.api", "admin.api"));
        InMemoryRegisteredClientRepository repository =
                new InMemoryRegisteredClientRepository(unrelatedClient());

        InitializingBean initializer = configuration.bootstrapRegisteredClient(
                repository,
                passwordEncoder,
                properties,
                hostAccessTokenProperties());
        initializer.afterPropertiesSet();

        RegisteredClient registeredClient =
                repository.findByClientId(properties.getInternalTokenClient().getClientId());
        assertNotNull(registeredClient);
        assertEquals(
                "user-auth internal token client",
                registeredClient.getClientName());
        assertEquals(
                Set.of(ClientAuthenticationMethod.CLIENT_SECRET_BASIC),
                registeredClient.getClientAuthenticationMethods());
        assertEquals(
                Set.of(
                        AuthorizationGrantType.REFRESH_TOKEN,
                        ExternalIdentityGrantTypes.EXTERNAL_IDENTITY,
                        MobileOtpGrantTypes.MOBILE_OTP),
                registeredClient.getAuthorizationGrantTypes());
        assertEquals(properties.getScopes(), registeredClient.getScopes());
        assertTrue(passwordEncoder.matches(
                clientSecret,
                registeredClient.getClientSecret()));
        assertFalse(clientSecret.equals(
                registeredClient.getClientSecret()));
        assertFalse(registeredClient.getTokenSettings().isReuseRefreshTokens());
        assertEquals(properties.getRefreshToken().getTtl(),
                registeredClient.getTokenSettings().getRefreshTokenTimeToLive());
        assertEquals(Duration.ofMinutes(15),
                registeredClient.getTokenSettings().getAccessTokenTimeToLive());
    }

    @Test
    void shouldReplaceUnexpectedMethodsAndGrantsOnExistingInternalClient() throws Exception {
        SasRegisteredClientConfiguration configuration =
                new SasRegisteredClientConfiguration();
        PasswordEncoder passwordEncoder = configuration.passwordEncoder();
        SasAuthorizationServerProperties properties =
                SasAuthorizationServerPropertiesFixtures.withInternalClient(
                        "user-auth-internal-test",
                        "new-secret");
        properties.getScopes().addAll(Set.of("app.api", "admin.api"));
        RegisteredClient existingClient = RegisteredClient.withId("existing-id")
                .clientId(properties.getInternalTokenClient().getClientId())
                .clientSecret(passwordEncoder.encode("old-secret"))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("unexpected.scope")
                .build();
        InMemoryRegisteredClientRepository repository =
                new InMemoryRegisteredClientRepository(existingClient);

        configuration.bootstrapRegisteredClient(
                repository,
                passwordEncoder,
                properties,
                hostAccessTokenProperties()).afterPropertiesSet();

        RegisteredClient registeredClient =
                repository.findByClientId(properties.getInternalTokenClient().getClientId());
        assertEquals("existing-id", registeredClient.getId());
        assertEquals(
                Set.of(ClientAuthenticationMethod.CLIENT_SECRET_BASIC),
                registeredClient.getClientAuthenticationMethods());
        assertEquals(
                Set.of(
                        AuthorizationGrantType.REFRESH_TOKEN,
                        ExternalIdentityGrantTypes.EXTERNAL_IDENTITY,
                        MobileOtpGrantTypes.MOBILE_OTP),
                registeredClient.getAuthorizationGrantTypes());
        assertEquals(properties.getScopes(), registeredClient.getScopes());
        assertTrue(passwordEncoder.matches(
                "new-secret",
                registeredClient.getClientSecret()));
    }

    private RegisteredClient unrelatedClient() {
        return RegisteredClient.withId("unrelated-id")
                .clientId("unrelated-client")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("unrelated.scope")
                .build();
    }

    private static AccessTokenProperties hostAccessTokenProperties() {
        AccessTokenProperties properties = new AccessTokenProperties();
        properties.setTtl(Duration.ofMinutes(15));
        return properties;
    }
}
