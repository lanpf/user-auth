package com.cloud.userauth.infrastructure.session.redis.config;

import com.cloud.framework.core.naming.NamespaceResolver;
import com.cloud.framework.core.naming.NamespacedResourceNameResolver;
import com.cloud.userauth.application.port.BrowserSessionStore;
import com.cloud.userauth.domain.authentication.session.LoginSessionRepository;
import com.cloud.userauth.infrastructure.session.redis.RedisBrowserSessionKeyResolver;
import com.cloud.userauth.infrastructure.session.redis.RedisBrowserSessionStore;
import java.time.Clock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(BrowserSessionProperties.class)
public class RedisBrowserSessionConfiguration {
    @Bean
    RedisBrowserSessionKeyResolver browserSessionKeyResolver(
            BrowserSessionProperties properties,
            NamespaceResolver namespaceResolver
    ) {
        return new RedisBrowserSessionKeyResolver(
                new NamespacedResourceNameResolver(namespaceResolver, properties));
    }

    @Bean
    BrowserSessionStore browserSessionStore(
            BrowserSessionProperties properties,
            StringRedisTemplate redisTemplate,
            RedisBrowserSessionKeyResolver keyResolver,
            LoginSessionRepository loginSessionRepository,
            Clock clock
    ) {
        return new RedisBrowserSessionStore(
                properties,
                redisTemplate,
                keyResolver,
                loginSessionRepository,
                clock);
    }
}
