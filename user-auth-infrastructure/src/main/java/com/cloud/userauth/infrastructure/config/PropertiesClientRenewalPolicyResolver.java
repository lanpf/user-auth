package com.cloud.userauth.infrastructure.config;

import com.cloud.userauth.application.common.ApplicationError;
import com.cloud.userauth.application.common.ApplicationException;
import com.cloud.userauth.application.port.ClientRenewalPolicy;
import com.cloud.userauth.application.port.ClientRenewalPolicyResolver;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class PropertiesClientRenewalPolicyResolver implements ClientRenewalPolicyResolver {
    private final ClientAppRegistryProperties properties;

    @Override
    public ClientRenewalPolicy resolve(String clientAppId) {
        ClientAppRegistryProperties.ClientAppProperties clientApp =
                properties.getClientApps().get(clientAppId);
        if (clientApp == null) {
            throw new ApplicationException(ApplicationError.APP_LOGIN_CLIENT_NOT_ALLOWED);
        }
        return clientApp.getRenewalPolicy();
    }
}
