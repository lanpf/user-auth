package com.cloud.userauth.infrastructure.oauth2.sas.scope;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cloud.userauth.api.authentication.OAuth2Scope;
import com.cloud.userauth.application.common.ApplicationError;
import com.cloud.userauth.application.common.ApplicationException;
import com.cloud.userauth.infrastructure.config.ClientAppRegistryProperties;
import com.cloud.userauth.application.port.ClientRenewalPolicy;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

class SasClientScopeResolverTest {
    private final PropertiesSasClientScopeResolver resolver =
            new PropertiesSasClientScopeResolver(properties());

    @Test
    void shouldResolveConfiguredClientScope() {
        assertEquals(Set.of("app"), resolver.resolve("mini-program"));
        assertEquals(Set.of("app"), resolver.resolve("app"));
        assertEquals(Set.of("admin"), resolver.resolve("admin"));
    }

    @Test
    void shouldRejectUnknownClient() {
        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> resolver.resolve("unknown-client"));

        assertEquals(
                ApplicationError.APP_LOGIN_CLIENT_NOT_ALLOWED.errorCode(),
                exception.getErrorCode());
    }

    private static ClientAppRegistryProperties properties() {
        ClientAppRegistryProperties properties = new ClientAppRegistryProperties();
        properties.getClientApps().putAll(Map.of(
                "mini-program", clientApp(Set.of(OAuth2Scope.APP)),
                "app", clientApp(Set.of(OAuth2Scope.APP)),
                "admin", clientApp(Set.of(OAuth2Scope.ADMIN))));
        return properties;
    }

    private static ClientAppRegistryProperties.ClientAppProperties clientApp(
            Set<OAuth2Scope> scopes
    ) {
        ClientAppRegistryProperties.ClientAppProperties properties =
                new ClientAppRegistryProperties.ClientAppProperties();
        properties.setRenewalPolicy(ClientRenewalPolicy.NONE);
        properties.getOauth2Scopes().addAll(scopes);
        return properties;
    }
}
