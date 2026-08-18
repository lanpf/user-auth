package com.cloud.userauth.infrastructure.session.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RedisSessionKeyResolverTest {
    @Test
    void shouldResolveBrowserSessionKeysWithinServiceNamespace() {
        RedisBrowserSessionKeyResolver resolver =
                new RedisBrowserSessionKeyResolver(name -> "user-auth:" + name);

        assertEquals("user-auth:browser-session:credential", resolver.key("credential"));
        assertEquals(
                "user-auth:browser-session:parent:login-session",
                resolver.parent("login-session"));
    }

    @Test
    void shouldResolveSessionHandoffKeysWithinServiceNamespace() {
        RedisSessionHandoffKeyResolver resolver =
                new RedisSessionHandoffKeyResolver(name -> "user-auth:" + name);

        assertEquals(
                "user-auth:session-handoff:ticket:ticket",
                resolver.ticket("ticket"));
        assertEquals(
                "user-auth:session-handoff:login-session:login-session",
                resolver.loginSession("login-session"));
    }
}
