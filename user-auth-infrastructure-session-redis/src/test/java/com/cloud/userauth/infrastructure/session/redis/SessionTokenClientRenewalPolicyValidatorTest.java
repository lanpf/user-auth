package com.cloud.userauth.infrastructure.session.redis;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cloud.userauth.application.port.ClientRenewalPolicy;
import com.cloud.userauth.infrastructure.config.ClientAppRegistryProperties;
import com.cloud.userauth.infrastructure.session.redis.config.SessionTokenClientRenewalPolicyValidator;
import org.junit.jupiter.api.Test;

class SessionTokenClientRenewalPolicyValidatorTest {
    @Test
    void shouldAcceptExternalAuthorizationCodeAndNoneForSessionToken() {
        ClientAppRegistryProperties properties = new ClientAppRegistryProperties();
        properties.getClientApps().put(
                "mini-program", clientApp(ClientRenewalPolicy.EXTERNAL_AUTHORIZATION_CODE));
        properties.getClientApps().put("admin", clientApp(ClientRenewalPolicy.NONE));

        assertDoesNotThrow(() ->
                new SessionTokenClientRenewalPolicyValidator(properties).afterPropertiesSet());
    }

    @Test
    void shouldRejectRefreshTokenRotationForSessionToken() {
        ClientAppRegistryProperties properties = new ClientAppRegistryProperties();
        properties.getClientApps().put(
                "app", clientApp(ClientRenewalPolicy.REFRESH_TOKEN_ROTATION));

        assertThrows(IllegalStateException.class, () ->
                new SessionTokenClientRenewalPolicyValidator(properties).afterPropertiesSet());
    }

    private static ClientAppRegistryProperties.ClientAppProperties clientApp(
            ClientRenewalPolicy renewalPolicy
    ) {
        ClientAppRegistryProperties.ClientAppProperties properties =
                new ClientAppRegistryProperties.ClientAppProperties();
        properties.setRenewalPolicy(renewalPolicy);
        return properties;
    }
}
