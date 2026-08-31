package com.cloud.userauth.infrastructure.session.redis;

import com.cloud.framework.core.naming.ResourceNameResolver;
import com.cloud.framework.starter.autoconfigure.naming.AbstractKeyResolver;

public class BrowserSessionKeyResolver extends AbstractKeyResolver {
    public BrowserSessionKeyResolver(ResourceNameResolver resourceNameResolver) {
        super(resourceNameResolver);
    }

    @Override
    protected String[] prefixes() {
        return new String[]{"browser-session"};
    }

    String key(String key) {
        return resolve(key);
    }

    String parent(String key) {
        return resolve("parent", key);
    }
}
