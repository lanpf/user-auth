package com.cloud.userauth.infrastructure.oauth2.sas.config.validation;

import com.cloud.userauth.infrastructure.config.ClientAppRegistryProperties;
import com.cloud.userauth.infrastructure.oauth2.sas.config.SasAuthorizationServerProperties;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.CollectionUtils;
import org.springframework.util.Assert;

@RequiredArgsConstructor
public final class SasClientScopeConfigurationValidator implements InitializingBean {
    private final SasAuthorizationServerProperties sasProperties;
    private final ClientAppRegistryProperties clientAppProperties;

    @Override
    public void afterPropertiesSet() {
        boolean invalid = clientAppProperties.getClientApps().values().stream()
                .filter(Objects::nonNull)
                .map(ClientAppRegistryProperties.ClientAppProperties::getOauth2Scopes)
                .anyMatch(configured -> CollectionUtils.isEmpty(configured)
                        || !sasProperties.getScopes().containsAll(configured));
        Assert.state(!invalid,
                "Every ClientApp OAuth2 scope set must be non-empty and contained in allowed SAS scopes");
    }
}
