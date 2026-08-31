package com.cloud.userauth.infrastructure.oauth2.redis;

import com.cloud.framework.core.naming.ResourceNameResolver;
import com.cloud.framework.starter.autoconfigure.naming.AbstractKeyResolver;

public class OAuth2AuthorizationRedisKeyResolver extends AbstractKeyResolver {
    public OAuth2AuthorizationRedisKeyResolver(ResourceNameResolver resourceNameResolver) {
        super(resourceNameResolver);
    }

    @Override
    protected String[] prefixes() {
        return new String[]{"oauth2", "{authorization-state}"};
    }

    String authorization(String authorizationId) {
        return resolve("authorization", authorizationId);
    }

    String accessToken(String tokenHash) {
        return resolve("access-token", nullableKeyPart(tokenHash));
    }

    String refreshToken(String tokenHash) {
        return resolve("refresh-token", nullableKeyPart(tokenHash));
    }

    String refreshTokenHistory(String tokenHash) {
        return resolve("refresh-token-history", nullableKeyPart(tokenHash));
    }

    private static String nullableKeyPart(String value) {
        return value == null ? "none" : value;
    }
}
