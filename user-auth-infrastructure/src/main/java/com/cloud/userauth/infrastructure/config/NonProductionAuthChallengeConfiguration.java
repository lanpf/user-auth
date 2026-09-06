package com.cloud.userauth.infrastructure.config;

import com.cloud.userauth.application.port.AuthChallengeDispatcher;
import com.cloud.userauth.application.port.OneTimeCodeGenerator;
import com.cloud.userauth.infrastructure.challenge.FixedOneTimeCodeGenerator;
import com.cloud.userauth.infrastructure.challenge.NoopAuthChallengeDispatcher;
import com.cloud.userauth.infrastructure.security.SensitiveValueCipher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.util.StringUtils;

@Configuration
@Profile("!prod")
@EnableConfigurationProperties(NonProductionAuthChallengeProperties.class)
public class NonProductionAuthChallengeConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public OneTimeCodeGenerator oneTimeCodeGenerator(NonProductionAuthChallengeProperties properties) {
        return new FixedOneTimeCodeGenerator(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthChallengeDispatcher authChallengeDispatcher(SensitiveDataProperties sensitiveDataProperties) {
        String key = sensitiveDataProperties.getCipherKey();
        return new NoopAuthChallengeDispatcher(
                StringUtils.hasText(key) ? new SensitiveValueCipher(key) : null);
    }
}
