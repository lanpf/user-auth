package com.cloud.userauth.infrastructure.session.redis;

import com.cloud.framework.core.naming.ResourceNameResolver;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RedisSessionHandoffKeyResolver {
    private static final String SCENE = "session-handoff";

    private final ResourceNameResolver resourceNameResolver;

    String ticket(String ticket) {
        return resolve("ticket", ticket);
    }

    String loginSession(String loginSessionId) {
        return resolve("login-session", loginSessionId);
    }

    private String resolve(String type, String key) {
        return resourceNameResolver.resolve(
                SCENE + ":" + type + ":" + key);
    }
}
