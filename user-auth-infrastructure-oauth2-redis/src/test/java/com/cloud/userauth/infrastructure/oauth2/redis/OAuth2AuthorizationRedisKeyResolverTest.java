package com.cloud.userauth.infrastructure.oauth2.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.cloud.framework.core.naming.NamespacedResourceNameResolver;
import com.cloud.userauth.infrastructure.oauth2.redis.config.OAuth2AuthorizationStoreProperties;
import org.junit.jupiter.api.Test;

class OAuth2AuthorizationRedisKeyResolverTest {

    @Test
    void shouldResolveNamespaceAndDefaultOAuth2Scene() {
        OAuth2AuthorizationStoreProperties properties =
                new OAuth2AuthorizationStoreProperties();
        properties.setNamespace("user-auth");
        OAuth2AuthorizationRedisKeyResolver resolver =
                new OAuth2AuthorizationRedisKeyResolver(
                        new NamespacedResourceNameResolver(
                                namespaced -> namespaced.getNamespace(),
                                properties),
                        properties.getScene());

        assertEquals(
                "user-auth:oauth2:{authorization-state}:authorization:authorization-1",
                resolver.authorization("authorization-1"));
        assertEquals(
                "user-auth:oauth2:{authorization-state}:access-token:access-hash",
                resolver.accessToken("access-hash"));
        assertEquals(
                "user-auth:oauth2:{authorization-state}:refresh-token:refresh-hash",
                resolver.refreshToken("refresh-hash"));
        assertEquals(
                "user-auth:oauth2:{authorization-state}:refresh-token-history:refresh-hash",
                resolver.refreshTokenHistory("refresh-hash"));
    }

    @Test
    void shouldUseNamespaceResolverFallbackWhenNamespaceIsMissing() {
        OAuth2AuthorizationStoreProperties properties =
                new OAuth2AuthorizationStoreProperties();
        OAuth2AuthorizationRedisKeyResolver resolver =
                new OAuth2AuthorizationRedisKeyResolver(
                        new NamespacedResourceNameResolver(
                                namespaced -> "user-auth",
                                properties),
                        properties.getScene());

        assertEquals(
                "user-auth:oauth2:{authorization-state}:authorization:authorization-1",
                resolver.authorization("authorization-1"));
    }

    @Test
    void shouldLetRedisImplementationComposeConfiguredScene() {
        OAuth2AuthorizationStoreProperties properties =
                new OAuth2AuthorizationStoreProperties();
        properties.setNamespace("user-auth");
        properties.setScene("oauth2-login");
        OAuth2AuthorizationRedisKeyResolver resolver =
                new OAuth2AuthorizationRedisKeyResolver(
                        new NamespacedResourceNameResolver(
                                namespaced -> namespaced.getNamespace(),
                                properties),
                        properties.getScene());

        assertEquals(
                "user-auth:oauth2-login:{authorization-state}:authorization:authorization-1",
                resolver.authorization("authorization-1"));
    }
}
