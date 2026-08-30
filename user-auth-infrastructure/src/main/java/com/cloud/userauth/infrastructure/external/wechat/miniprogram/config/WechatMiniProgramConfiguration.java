package com.cloud.userauth.infrastructure.external.wechat.miniprogram.config;

import com.cloud.userauth.infrastructure.external.wechat.miniprogram.client.WechatMiniProgramClientConfiguration;
import com.cloud.userauth.infrastructure.external.wechat.miniprogram.verification.WechatMiniProgramVerificationConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(
        prefix = WechatMiniProgramProperties.PREFIX,
        name = "enabled",
        havingValue = "true")
@EnableConfigurationProperties(WechatMiniProgramProperties.class)
@Import({
        WechatMiniProgramClientConfiguration.class,
        WechatMiniProgramVerificationConfiguration.class
})
public class WechatMiniProgramConfiguration {
}
