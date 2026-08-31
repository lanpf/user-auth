package com.cloud.userauth.infrastructure.session.redis;

import com.cloud.framework.core.naming.ResourceNameResolver;
import com.cloud.framework.starter.autoconfigure.naming.AbstractKeyResolver;

public class SessionHandoffKeyResolver extends AbstractKeyResolver {
    public SessionHandoffKeyResolver(ResourceNameResolver resourceNameResolver) {
        super(resourceNameResolver);
    }

    @Override
    protected String[] prefixes() {
        return new String[]{"session-handoff"};
    }

    String ticket(String ticket) {
        return resolve("ticket", ticket);
    }

    String loginSession(String loginSessionId) {
        return resolve("login-session", loginSessionId);
    }
}
