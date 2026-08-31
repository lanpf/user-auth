package com.cloud.userauth.infrastructure.session.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.cloud.framework.core.naming.NamespacedResourceNameResolver;
import com.cloud.userauth.infrastructure.session.redis.config.BrowserSessionProperties;
import com.cloud.userauth.infrastructure.session.redis.config.SessionHandoffProperties;
import org.junit.jupiter.api.Test;

class RedisSessionKeyResolverTest {
    @Test
    void shouldResolveBrowserSessionKeysWithinServiceNamespace() {
        BrowserSessionProperties properties = new BrowserSessionProperties();
        properties.setNamespace("user-auth");
        BrowserSessionKeyResolver resolver =
                new BrowserSessionKeyResolver(new NamespacedResourceNameResolver(
                        namespaced -> namespaced.getNamespace(),
                        properties));

        assertEquals("user-auth:browser-session:credential", resolver.key("credential"));
        assertEquals(
                "user-auth:browser-session:parent:login-session",
                resolver.parent("login-session"));
    }

    @Test
    void shouldResolveSessionHandoffKeysWithinServiceNamespace() {
        SessionHandoffProperties properties = new SessionHandoffProperties();
        properties.setNamespace("user-auth");
        SessionHandoffKeyResolver resolver =
                new SessionHandoffKeyResolver(new NamespacedResourceNameResolver(
                        namespaced -> namespaced.getNamespace(),
                        properties));

        assertEquals(
                "user-auth:session-handoff:ticket:ticket",
                resolver.ticket("ticket"));
        assertEquals(
                "user-auth:session-handoff:login-session:login-session",
                resolver.loginSession("login-session"));
    }
}
