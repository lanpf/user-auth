package com.cloud.userauth.infrastructure.session.redis.config;

import com.cloud.framework.core.naming.NamespaceResolver;
import com.cloud.framework.core.naming.NamespacedResourceNameResolver;
import com.cloud.userauth.application.port.SessionHandoffStore;
import com.cloud.userauth.infrastructure.session.redis.RedisSessionHandoffStore;
import com.cloud.userauth.infrastructure.session.redis.RedisSessionHandoffKeyResolver;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(SessionHandoffProperties.class)
public class RedisSessionHandoffConfiguration {
    @Bean
    RedisSessionHandoffKeyResolver sessionHandoffKeyResolver(
            SessionHandoffProperties properties,
            NamespaceResolver namespaceResolver
    ) {
        return new RedisSessionHandoffKeyResolver(
                new NamespacedResourceNameResolver(namespaceResolver, properties));
    }

    @Bean
    SessionHandoffStore sessionHandoffStore(
            StringRedisTemplate redisTemplate,
            RedisSessionHandoffKeyResolver keyResolver
    ) {
        return new RedisSessionHandoffStore(redisTemplate, keyResolver);
    }
}
