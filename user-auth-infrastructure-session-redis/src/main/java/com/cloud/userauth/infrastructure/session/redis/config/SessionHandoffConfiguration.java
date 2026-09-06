package com.cloud.userauth.infrastructure.session.redis.config;

import com.cloud.userauth.application.port.BrowserSessionStore;
import com.cloud.userauth.application.port.SessionHandoffStore;
import com.cloud.userauth.application.session.browser.EndBrowserSessionCommandService;
import com.cloud.userauth.application.session.browser.VerifyBrowserSessionCommandService;
import com.cloud.userauth.application.session.handoff.CreateSessionHandoffCommandService;
import com.cloud.userauth.application.session.handoff.ExchangeSessionHandoffCommandService;
import com.cloud.userauth.application.session.handoff.SessionHandoffService;
import com.cloud.userauth.domain.authentication.session.LoginSessionRepository;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
                handoffStore, loginSessionRepository, clock, handoffProperties.getTtl());
    }

    @Bean
    public ExchangeSessionHandoffCommandService exchangeSessionHandoffCommandService(
            SessionHandoffService handoffService,
            BrowserSessionStore sessionStore,
            BrowserSessionProperties sessionProperties
    ) {
        return new ExchangeSessionHandoffCommandService(
                handoffService,
                sessionStore,
                sessionProperties.getIdleTtl(),
                sessionProperties.getAbsoluteTtl());
    }

    @Bean
    public CreateSessionHandoffCommandService createSessionHandoffCommandService(
            SessionHandoffService handoffService
    ) {
        return new CreateSessionHandoffCommandService(handoffService);
    }

    @Bean
    public VerifyBrowserSessionCommandService verifyBrowserSessionCommandService(
            BrowserSessionStore sessionStore
    ) {
        return new VerifyBrowserSessionCommandService(sessionStore);
    }

    @Bean
    public EndBrowserSessionCommandService endBrowserSessionCommandService(
            BrowserSessionStore sessionStore
    ) {
        return new EndBrowserSessionCommandService(sessionStore);
    }
}
