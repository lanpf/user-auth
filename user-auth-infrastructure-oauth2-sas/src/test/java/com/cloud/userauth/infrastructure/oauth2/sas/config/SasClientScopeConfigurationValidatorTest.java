package com.cloud.userauth.infrastructure.oauth2.sas.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cloud.userauth.api.authentication.OAuth2Scope;
import com.cloud.userauth.application.port.ClientRenewalPolicy;
import com.cloud.userauth.infrastructure.config.ClientAppRegistryProperties;
import com.cloud.userauth.infrastructure.oauth2.sas.config.validation.SasClientScopeConfigurationValidator;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SasClientScopeConfigurationValidatorTest {
    @Test
    void shouldAcceptClientScopesWithinAllowedScopes() {
        SasAuthorizationServerProperties sas =
                sasProperties(Set.of(OAuth2Scope.APP, OAuth2Scope.ADMIN));
        ClientAppRegistryProperties clients =
                clientProperties(Set.of(OAuth2Scope.APP));

        assertDoesNotThrow(() ->
                new SasClientScopeConfigurationValidator(sas, clients).afterPropertiesSet());
    }

    @Test
    void shouldRejectClientScopeOutsideAllowedScopes() {
        SasAuthorizationServerProperties sas =
                sasProperties(Set.of(OAuth2Scope.APP));
        ClientAppRegistryProperties clients =
                clientProperties(Set.of(OAuth2Scope.ADMIN));

        assertThrows(IllegalStateException.class, () ->
                new SasClientScopeConfigurationValidator(sas, clients).afterPropertiesSet());
    }

    private static SasAuthorizationServerProperties sasProperties(
            Set<OAuth2Scope> scopes
    ) {
        SasAuthorizationServerProperties properties = new SasAuthorizationServerProperties();
        properties.getScopes().addAll(scopes);
        return properties;
    }

    private static ClientAppRegistryProperties clientProperties(
            Set<OAuth2Scope> scopes
    ) {
        ClientAppRegistryProperties properties = new ClientAppRegistryProperties();
        ClientAppRegistryProperties.ClientAppProperties client =
                new ClientAppRegistryProperties.ClientAppProperties();
        client.setRenewalPolicy(ClientRenewalPolicy.NONE);
        client.getOauth2Scopes().addAll(scopes);
        properties.getClientApps().put("app", client);
        return properties;
    }
}
