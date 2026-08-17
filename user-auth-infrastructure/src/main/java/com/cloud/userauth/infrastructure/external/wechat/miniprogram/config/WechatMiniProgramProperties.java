package com.cloud.userauth.infrastructure.external.wechat.miniprogram.config;

import com.cloud.framework.core.http.RestClientProperties;
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
@ConfigurationProperties("user-auth.authentication.external-identity.wechat-mini-program")
public class WechatMiniProgramProperties {
    private boolean enabled;
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
