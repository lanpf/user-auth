package com.cloud.userauth.infrastructure.session.redis;

import com.cloud.framework.core.naming.ResourceNameResolver;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RedisBrowserSessionKeyResolver {
    private static final String SCENE = "browser-session";
    private static final String PARENT = "parent";

    private final ResourceNameResolver resourceNameResolver;


    String key(String key) {
        return resourceNameResolver.resolve(SCENE + ":" + key);
    }

    String parent(String key) {
        return resourceNameResolver.resolve(SCENE + ":" + PARENT + ":" + key);
    }
}
