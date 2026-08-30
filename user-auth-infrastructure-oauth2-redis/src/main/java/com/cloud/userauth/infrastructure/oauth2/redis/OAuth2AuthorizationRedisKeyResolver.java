package com.cloud.userauth.infrastructure.oauth2.redis;

import com.cloud.framework.core.naming.ResourceNameResolver;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OAuth2AuthorizationRedisKeyResolver {
    private static final String SCENE = "oauth2";
    private static final String AUTHORIZATION_STATE_HASH_TAG = "{authorization-state}";

    private final ResourceNameResolver resourceNameResolver;

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

    private String resolve(String type, String key) {
        return resourceNameResolver.resolve(
                SCENE + ":" + AUTHORIZATION_STATE_HASH_TAG + ":" + type + ":" + key);
    }

    private static String nullableKeyPart(String value) {
        return value == null ? "none" : value;
    }
}
