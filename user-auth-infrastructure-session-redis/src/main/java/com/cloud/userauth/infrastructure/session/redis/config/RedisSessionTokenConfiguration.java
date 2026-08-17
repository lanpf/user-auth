package com.cloud.userauth.infrastructure.session.redis.config;

import com.cloud.userauth.application.login.MobileOtpAuthenticationProcess;
import com.cloud.userauth.application.login.external.ExternalAuthenticationProcess;
import com.cloud.userauth.application.port.SessionTokenStore;
import com.cloud.userauth.application.port.LoginTokenIssuer;
import com.cloud.userauth.application.port.LoginTokenRefresher;
import com.cloud.userauth.domain.authentication.session.LoginSessionRepository;
import com.cloud.userauth.infrastructure.session.redis.RedisSessionTokenStore;
import com.cloud.userauth.infrastructure.session.redis.login.SessionTokenLoginTokenIssuer;
import com.cloud.userauth.infrastructure.session.redis.login.SessionTokenLoginTokenRefresher;
import com.cloud.userauth.infrastructure.config.ClientAppRegistryProperties;
import com.cloud.userauth.infrastructure.config.AccessTokenProperties;
import java.time.Clock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(
        prefix = "user-auth.authentication.access-token",
        name = "provider",
        havingValue = "session-token")
public class RedisSessionTokenConfiguration {
    @Bean
    public SessionTokenStore sessionTokenStore(
            StringRedisTemplate redisTemplate,
            LoginSessionRepository loginSessionRepository,
            Clock clock
    ) {
        return new RedisSessionTokenStore(
                redisTemplate, loginSessionRepository, clock);
    }

    @Bean
    public LoginTokenIssuer sessionTokenLoginTokenIssuer(
            MobileOtpAuthenticationProcess mobileOtpAuthenticationProcess,
            ExternalAuthenticationProcess externalAuthenticationProcess,
            SessionTokenStore sessionTokenStore,
            AccessTokenProperties properties
    ) {
        return new SessionTokenLoginTokenIssuer(
                mobileOtpAuthenticationProcess,
                externalAuthenticationProcess,
                sessionTokenStore,
                properties.getTtl());
    }

    @Bean
    public LoginTokenRefresher sessionTokenLoginTokenRefresher() {
        return new SessionTokenLoginTokenRefresher();
    }

    @Bean
    public SessionTokenClientRenewalPolicyValidator sessionTokenClientRenewalPolicyValidator(
            ClientAppRegistryProperties properties
    ) {
        return new SessionTokenClientRenewalPolicyValidator(properties);
    }
}
