package com.cloud.userauth.infrastructure.oauth2.sas.config.validation;

import com.cloud.userauth.infrastructure.config.ClientAppRegistryProperties;
import com.cloud.userauth.infrastructure.oauth2.sas.config.SasAuthorizationServerProperties;
import java.util.Objects;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.CollectionUtils;

public final class SasClientScopeConfigurationValidator implements InitializingBean {
    private final SasAuthorizationServerProperties sasProperties;
    private final ClientAppRegistryProperties clientAppProperties;

    public SasClientScopeConfigurationValidator(
            SasAuthorizationServerProperties sasProperties,
            ClientAppRegistryProperties clientAppProperties
    ) {
        this.sasProperties = sasProperties;
        this.clientAppProperties = clientAppProperties;
    }

    @Override
    public void afterPropertiesSet() {
        if (CollectionUtils.isEmpty(sasProperties.getScopes())) {
            throw new IllegalStateException("OAuth2 allowed scopes must not be empty");
        }
        boolean invalid = clientAppProperties.getClientApps().values().stream()
                .filter(Objects::nonNull)
                .map(ClientAppRegistryProperties.ClientAppProperties::getOauth2Scopes)
                .anyMatch(configured -> CollectionUtils.isEmpty(configured)
                        || !sasProperties.getScopes().containsAll(configured));
        if (invalid) {
            throw new IllegalStateException(
                    "Every ClientApp OAuth2 scope set must be non-empty and contained in allowed SAS scopes");
        }
    }
}
