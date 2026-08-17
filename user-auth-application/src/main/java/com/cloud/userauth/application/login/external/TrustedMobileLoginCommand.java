package com.cloud.userauth.application.login.external;

import jakarta.validation.constraints.NotBlank;

/** 网关已验证合作方授权证明后的直连登录命令。 */
public record TrustedMobileLoginCommand(
        @NotBlank String issuer,
        @NotBlank String authorizationCode,
        @NotBlank String mobile,
        String deviceId,
        String deviceType,
        String deviceName,
        @NotBlank String clientAppId,
        String clientPlatform,
        String clientVersion,
        String channelCode
) {
}
