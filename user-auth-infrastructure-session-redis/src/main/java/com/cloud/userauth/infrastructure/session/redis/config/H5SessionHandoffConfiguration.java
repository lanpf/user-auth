package com.cloud.userauth.infrastructure.session.redis.config;

import com.cloud.userauth.application.port.H5SessionStore;
import com.cloud.userauth.application.session.handoff.H5SessionHandoffCommandService;
import com.cloud.userauth.application.session.handoff.SessionHandoffTicketService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class H5SessionHandoffConfiguration {
    @Bean
    public H5SessionHandoffCommandService h5SessionHandoffCommandService(
            SessionHandoffTicketService ticketService,
            H5SessionStore h5SessionStore,
            H5SessionProperties properties
    ) {
        return new H5SessionHandoffCommandService(
                ticketService,
                h5SessionStore,
                properties.getIdleTtl(),
                properties.getAbsoluteTtl());
    }
}
