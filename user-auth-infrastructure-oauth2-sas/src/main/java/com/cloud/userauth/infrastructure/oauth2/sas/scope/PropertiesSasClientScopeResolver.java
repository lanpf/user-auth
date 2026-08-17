package com.cloud.userauth.infrastructure.oauth2.sas.scope;

import com.cloud.userauth.application.common.ApplicationError;
import com.cloud.userauth.application.common.ApplicationException;
import com.cloud.userauth.infrastructure.config.ClientAppRegistryProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 根据公开登录请求中的客户端标识选择服务端配置的 OAuth2 scope。
 */
@RequiredArgsConstructor
public final class PropertiesSasClientScopeResolver implements SasClientScopeResolver {
    private final ClientAppRegistryProperties properties;

    @Override
    public Set<String> resolve(String clientAppId) {
        ClientAppRegistryProperties.ClientAppProperties clientApp =
                properties.getClientApps().get(clientAppId);
        if (clientApp == null || CollectionUtils.isEmpty(clientApp.getOauth2Scopes())) {
            throw new ApplicationException(
                    ApplicationError.APP_LOGIN_CLIENT_NOT_ALLOWED);
        }
        return Collections.unmodifiableSet(new LinkedHashSet<>(clientApp.getOauth2Scopes()));
    }
}
