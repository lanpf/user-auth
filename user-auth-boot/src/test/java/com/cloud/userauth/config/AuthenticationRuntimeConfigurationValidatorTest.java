package com.cloud.userauth.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cloud.userauth.infrastructure.config.AccessTokenProperties;
import org.junit.jupiter.api.Test;

class AuthenticationRuntimeConfigurationValidatorTest {
    @Test
    void shouldAllowSessionTokenWithoutAuthorizationServer() {
        assertDoesNotThrow(() -> validator(
                AccessTokenProperties.Provider.SESSION_TOKEN, false).afterPropertiesSet());
    }

    @Test
    void shouldAllowSessionTokenWithAuthorizationServer() {
        assertDoesNotThrow(() -> validator(
                AccessTokenProperties.Provider.SESSION_TOKEN, true).afterPropertiesSet());
    }

    @Test
    void shouldRequireAuthorizationServerForSasAccessToken() {
        AuthenticationRuntimeConfigurationValidator validator = validator(
                AccessTokenProperties.Provider.SAS, false);

        assertThrows(IllegalStateException.class, validator::afterPropertiesSet);
    }

    private static AuthenticationRuntimeConfigurationValidator validator(
            AccessTokenProperties.Provider provider,
            boolean authorizationServerEnabled
    ) {
        AuthenticationRuntimeProperties runtimeProperties = new AuthenticationRuntimeProperties();
        runtimeProperties.getOauth2().getAuthorizationServer()
                .setEnabled(authorizationServerEnabled);
        AccessTokenProperties accessTokenProperties = new AccessTokenProperties();
        accessTokenProperties.setProvider(provider);
        return new AuthenticationRuntimeConfigurationValidator(
                runtimeProperties, accessTokenProperties);
    }
}
