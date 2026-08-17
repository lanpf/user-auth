package com.cloud.userauth.infrastructure.session.redis.config;

import com.cloud.userauth.application.port.H5SessionStore;
import com.cloud.userauth.domain.authentication.session.LoginSessionRepository;
import com.cloud.userauth.infrastructure.session.redis.RedisH5SessionStore;
import java.time.Clock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(H5SessionProperties.class)
public class RedisH5SessionConfiguration {
    @Bean
    public H5SessionStore h5SessionStore(
            H5SessionProperties properties,
            StringRedisTemplate redisTemplate,
            LoginSessionRepository loginSessionRepository,
            Clock clock
    ) {
        return new RedisH5SessionStore(
                properties,redisTemplate, loginSessionRepository, clock);
    }
}
