package com.cloud.userauth.infrastructure.oauth2.sas.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cloud.userauth.application.port.ClientRenewalPolicy;
import com.cloud.userauth.infrastructure.config.ClientAppRegistryProperties;
import com.cloud.userauth.infrastructure.oauth2.sas.config.validation.SasClientScopeConfigurationValidator;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SasClientScopeConfigurationValidatorTest {
    @Test
    void shouldAcceptClientScopesWithinAllowedScopes() {
        SasAuthorizationServerProperties sas = sasProperties(Set.of("app.api", "admin.api"));
        ClientAppRegistryProperties clients = clientProperties(Set.of("app.api"));

        assertDoesNotThrow(() ->
                new SasClientScopeConfigurationValidator(sas, clients).afterPropertiesSet());
    }

    @Test
    void shouldRejectClientScopeOutsideAllowedScopes() {
        SasAuthorizationServerProperties sas = sasProperties(Set.of("app.api"));
        ClientAppRegistryProperties clients = clientProperties(Set.of("unknown.api"));

        assertThrows(IllegalStateException.class, () ->
                new SasClientScopeConfigurationValidator(sas, clients).afterPropertiesSet());
    }

    private static SasAuthorizationServerProperties sasProperties(Set<String> scopes) {
        SasAuthorizationServerProperties properties = new SasAuthorizationServerProperties();
        properties.getScopes().addAll(scopes);
        return properties;
    }

    private static ClientAppRegistryProperties clientProperties(Set<String> scopes) {
        ClientAppRegistryProperties properties = new ClientAppRegistryProperties();
        ClientAppRegistryProperties.ClientAppProperties client =
                new ClientAppRegistryProperties.ClientAppProperties();
        client.setRenewalPolicy(ClientRenewalPolicy.NONE);
        client.getOauth2Scopes().addAll(scopes);
        properties.getClientApps().put("app", client);
        return properties;
    }
}
