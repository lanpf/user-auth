package com.cloud.userauth.infrastructure.external.wechat.miniprogram.config;

import com.cloud.framework.core.http.RestClientProperties;
import com.cloud.userauth.infrastructure.config.ExternalIdentityProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(WechatMiniProgramProperties.PREFIX)
public class WechatMiniProgramProperties {
    public static final String PREFIX = ExternalIdentityProperties.PREFIX + ".wechat-mini-program";
    private boolean enabled;
    @NotBlank
    private String baseUrl = "https://api.weixin.qq.com";
    @NotBlank
    private String appId;
    @NotBlank
    private String appSecret;
    @NotNull
    @DurationMin(
            seconds = 1,
            message = "wechat mini-program access token refresh skew must be at least 1 second")
    private Duration accessTokenRefreshSkew = Duration.ofMinutes(1);

    @Valid
    private final RestClientProperties restClient = new RestClientProperties();
}
