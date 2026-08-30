package com.cloud.userauth.infrastructure.external.wechat.miniprogram.verification;

import com.cloud.userauth.application.port.ExternalIdentityVerifier;
import com.cloud.userauth.infrastructure.external.wechat.miniprogram.client.WechatMiniProgramClient;
import com.cloud.userauth.infrastructure.external.wechat.miniprogram.config.WechatMiniProgramProperties;
import jakarta.validation.Validator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(
        prefix = WechatMiniProgramProperties.PREFIX,
        name = "enabled",
        havingValue = "true")
public class WechatMiniProgramVerificationConfiguration {

    @Bean
    @ConditionalOnMissingBean(
            name = "wechatMiniProgramExternalIdentityVerifier")
    ExternalIdentityVerifier wechatMiniProgramExternalIdentityVerifier(
            WechatMiniProgramClient client,
            Validator validator
    ) {
        return new WechatMiniProgramExternalIdentityVerifier(
                client,
                validator);
    }
}
