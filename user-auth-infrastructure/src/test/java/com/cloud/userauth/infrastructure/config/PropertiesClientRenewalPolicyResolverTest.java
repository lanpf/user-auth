package com.cloud.userauth.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cloud.userauth.application.common.ApplicationError;
import com.cloud.userauth.application.common.ApplicationException;
import com.cloud.userauth.application.port.ClientRenewalPolicy;
import org.junit.jupiter.api.Test;

class PropertiesClientRenewalPolicyResolverTest {
    @Test
    void shouldResolveConfiguredClientAppRenewalPolicy() {
        ClientAppRegistryProperties properties = properties(
                "mini-program", ClientRenewalPolicy.EXTERNAL_AUTHORIZATION_CODE);

        assertEquals(
                ClientRenewalPolicy.EXTERNAL_AUTHORIZATION_CODE,
                new PropertiesClientRenewalPolicyResolver(properties).resolve("mini-program"));
    }

    @Test
    void shouldRejectUnknownClientApp() {
        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> new PropertiesClientRenewalPolicyResolver(
                        new ClientAppRegistryProperties()).resolve("unknown"));

        assertEquals(
                ApplicationError.APP_LOGIN_CLIENT_NOT_ALLOWED.errorCode(),
                exception.getErrorCode());
    }

    private static ClientAppRegistryProperties properties(
            String clientAppId,
            ClientRenewalPolicy renewalPolicy
    ) {
        ClientAppRegistryProperties properties = new ClientAppRegistryProperties();
        ClientAppRegistryProperties.ClientAppProperties clientApp =
                new ClientAppRegistryProperties.ClientAppProperties();
        clientApp.setRenewalPolicy(renewalPolicy);
        properties.getClientApps().put(clientAppId, clientApp);
        return properties;
    }
}
