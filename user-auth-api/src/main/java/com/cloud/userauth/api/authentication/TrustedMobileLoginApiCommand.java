package com.cloud.userauth.api.authentication;

import com.cloud.framework.core.Request;
import jakarta.validation.constraints.NotBlank;

/** 网关已验签的合作方可信手机号授权码登录请求。 */
public record TrustedMobileLoginApiCommand(
        @NotBlank String issuer,
        @NotBlank String authorizationCode,
        @NotBlank String mobile,
        @NotBlank String clientAppId,
        String clientPlatform,
        String clientVersion,
        @NotBlank String channelCode,
        String deviceId,
        String deviceType,
        String deviceName
) implements Request {
}
