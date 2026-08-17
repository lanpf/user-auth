package com.cloud.userauth.infrastructure.config;

import com.cloud.userauth.application.port.OneTimeCodeGenerator;
import com.cloud.userauth.infrastructure.challenge.SecureOneTimeCodeGenerator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("prod")
public class ProductionAuthChallengeConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public OneTimeCodeGenerator oneTimeCodeGenerator() {
        return new SecureOneTimeCodeGenerator();
    }
}
