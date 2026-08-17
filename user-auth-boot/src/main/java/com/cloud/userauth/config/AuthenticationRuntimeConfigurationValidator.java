package com.cloud.userauth.config;

import com.cloud.userauth.infrastructure.config.AccessTokenProperties;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
public final class AuthenticationRuntimeConfigurationValidator implements InitializingBean {
    private final AuthenticationRuntimeProperties runtimeProperties;
    private final AccessTokenProperties accessTokenProperties;

    public AuthenticationRuntimeConfigurationValidator(
            AuthenticationRuntimeProperties runtimeProperties,
            AccessTokenProperties accessTokenProperties
    ) {
        this.runtimeProperties = runtimeProperties;
        this.accessTokenProperties = accessTokenProperties;
    }

    @Override
    public void afterPropertiesSet() {
        if (accessTokenProperties.getProvider() == AccessTokenProperties.Provider.SAS
                && !runtimeProperties.getOauth2().getAuthorizationServer().isEnabled()) {
            throw new IllegalStateException(
                    "user-auth.authentication.oauth2.authorization-server.enabled must be true "
                            + "when user-auth.authentication.access-token.provider is sas");
        }
    }
}
