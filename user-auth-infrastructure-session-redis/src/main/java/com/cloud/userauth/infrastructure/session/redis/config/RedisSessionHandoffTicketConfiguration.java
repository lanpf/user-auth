package com.cloud.userauth.infrastructure.session.redis.config;

import com.cloud.userauth.application.port.SessionHandoffTicketStore;
import com.cloud.userauth.application.session.handoff.SessionHandoffTicketService;
import com.cloud.userauth.domain.authentication.session.LoginSessionRepository;
import com.cloud.userauth.infrastructure.session.redis.RedisSessionHandoffTicketStore;
import java.time.Clock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(SessionHandoffTicketProperties.class)
public class RedisSessionHandoffTicketConfiguration {
    @Bean
    public SessionHandoffTicketStore sessionHandoffTicketStore(
            StringRedisTemplate redisTemplate
    ) {
        return new RedisSessionHandoffTicketStore(redisTemplate);
    }

    @Bean
    public SessionHandoffTicketService sessionHandoffTicketService(
            SessionHandoffTicketStore ticketStore,
            SessionHandoffTicketProperties ticketProperties,
            LoginSessionRepository loginSessionRepository,
            Clock clock
    ) {
        return new SessionHandoffTicketService(
                ticketStore, ticketProperties.getTtl(), loginSessionRepository, clock);
    }
}
