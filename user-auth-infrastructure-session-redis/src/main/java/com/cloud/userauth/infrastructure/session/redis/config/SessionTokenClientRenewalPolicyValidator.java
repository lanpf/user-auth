package com.cloud.userauth.infrastructure.session.redis.config;

import com.cloud.userauth.application.port.ClientRenewalPolicy;
import com.cloud.userauth.infrastructure.config.ClientAppRegistryProperties;
import org.springframework.beans.factory.InitializingBean;

public final class SessionTokenClientRenewalPolicyValidator implements InitializingBean {
    private final ClientAppRegistryProperties properties;

    public SessionTokenClientRenewalPolicyValidator(ClientAppRegistryProperties properties) {
        this.properties = properties;
    }

    @Override
    public void afterPropertiesSet() {
        boolean refreshTokenConfigured = properties.getClientApps().values().stream()
                .anyMatch(clientApp -> clientApp.getRenewalPolicy()
                        == ClientRenewalPolicy.REFRESH_TOKEN_ROTATION);
        if (refreshTokenConfigured) {
            throw new IllegalStateException(
                    "REFRESH_TOKEN_ROTATION is not supported when "
                            + "user-auth.authentication.access-token.provider is session-token");
        }
    }
}
