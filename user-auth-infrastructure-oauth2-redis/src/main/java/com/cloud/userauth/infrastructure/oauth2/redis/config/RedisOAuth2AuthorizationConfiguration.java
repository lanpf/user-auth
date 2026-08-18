package com.cloud.userauth.infrastructure.oauth2.redis.config;

import com.cloud.framework.core.naming.NamespaceResolver;
import com.cloud.framework.core.naming.NamespacedResourceNameResolver;
import com.cloud.userauth.domain.authentication.session.LoginSessionRepository;
import com.cloud.userauth.infrastructure.oauth2.redis.OAuth2AuthorizationRedisKeyResolver;
import com.cloud.userauth.infrastructure.oauth2.redis.OAuth2AuthorizationRedisJsonMapper;
import com.cloud.userauth.infrastructure.oauth2.redis.OAuth2AuthorizationRedisStore;
import com.cloud.userauth.infrastructure.oauth2.redis.RedisOAuth2AuthorizationService;
import com.cloud.userauth.infrastructure.oauth2.redis.StringRedisOAuth2AuthorizationStore;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(OAuth2AuthorizationStoreProperties.class)
public class RedisOAuth2AuthorizationConfiguration {
    @Bean
    OAuth2AuthorizationRedisJsonMapper authorizationRedisJsonMapper(
            RegisteredClientRepository registeredClientRepository
    ) {
        return new OAuth2AuthorizationRedisJsonMapper(registeredClientRepository);
    }

    @Bean
    OAuth2AuthorizationRedisKeyResolver authorizationRedisKeyResolver(
            OAuth2AuthorizationStoreProperties properties,
            NamespaceResolver namespaceResolver
    ) {
        return new OAuth2AuthorizationRedisKeyResolver(
                new NamespacedResourceNameResolver(namespaceResolver, properties),
                properties.getScene());
    }

    @Bean
    OAuth2AuthorizationRedisStore authorizationRedisStore(
            StringRedisTemplate redisTemplate,
            OAuth2AuthorizationRedisJsonMapper jsonMapper,
            OAuth2AuthorizationRedisKeyResolver keyResolver,
            Clock clock
    ) {
        return new StringRedisOAuth2AuthorizationStore(
                redisTemplate,
                jsonMapper,
                keyResolver,
                clock);
    }

    @Bean
    OAuth2AuthorizationService authorizationService(
            OAuth2AuthorizationRedisStore store,
            LoginSessionRepository sessionRepository,
            Clock clock
    ) {
        return new RedisOAuth2AuthorizationService(store, sessionRepository, clock);
    }
}
