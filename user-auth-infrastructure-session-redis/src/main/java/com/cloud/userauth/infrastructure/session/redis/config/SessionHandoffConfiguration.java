package com.cloud.userauth.infrastructure.session.redis.config;

import com.cloud.userauth.application.port.BrowserSessionStore;
import com.cloud.userauth.application.port.SessionHandoffStore;
import com.cloud.userauth.application.session.handoff.SessionHandoffCommandService;
import com.cloud.userauth.application.session.handoff.SessionHandoffService;
import com.cloud.userauth.domain.authentication.session.LoginSessionRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration(proxyBeanMethods = false)
public class SessionHandoffConfiguration {
    @Bean
    SessionHandoffService sessionHandoffService(
            SessionHandoffStore handoffStore,
            SessionHandoffProperties handoffProperties,
            LoginSessionRepository loginSessionRepository,
            Clock clock
    ) {
        return new SessionHandoffService(
                handoffStore, handoffProperties.getTtl(), loginSessionRepository, clock);
    }

    @Bean
    public SessionHandoffCommandService sessionHandoffCommandService(
            SessionHandoffService handoffService,
            BrowserSessionStore sessionStore,
            BrowserSessionProperties sessionProperties
    ) {
        return new SessionHandoffCommandService(
                handoffService,
                sessionStore,
                sessionProperties.getIdleTtl(),
                sessionProperties.getAbsoluteTtl());
    }
}
