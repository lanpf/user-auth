package com.cloud.userauth.infrastructure.oauth2.sas.scope;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cloud.userauth.application.common.ApplicationError;
import com.cloud.userauth.application.common.ApplicationException;
import com.cloud.userauth.infrastructure.config.ClientAppRegistryProperties;
import com.cloud.userauth.application.port.ClientRenewalPolicy;
import java.util.Map;
import java.util.Set;

import com.cloud.userauth.infrastructure.oauth2.sas.scope.PropertiesSasClientScopeResolver;
import org.junit.jupiter.api.Test;

class SasClientScopeResolverTest {
    private final PropertiesSasClientScopeResolver resolver =
            new PropertiesSasClientScopeResolver(properties());

    @Test
    void shouldResolveConfiguredClientScope() {
        assertEquals(Set.of("app.api"), resolver.resolve("mini-program"));
        assertEquals(Set.of("app.api"), resolver.resolve("app"));
        assertEquals(Set.of("admin.api"), resolver.resolve("admin"));
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
                "mini-program", clientApp(Set.of("app.api")),
                "app", clientApp(Set.of("app.api")),
                "admin", clientApp(Set.of("admin.api"))));
        return properties;
    }

    private static ClientAppRegistryProperties.ClientAppProperties clientApp(Set<String> scopes) {
        ClientAppRegistryProperties.ClientAppProperties properties =
                new ClientAppRegistryProperties.ClientAppProperties();
        properties.setRenewalPolicy(ClientRenewalPolicy.NONE);
        properties.getOauth2Scopes().addAll(scopes);
        return properties;
    }
}
