package com.cloud.userauth.application.login;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 手机号身份认证流程输入。
 * 不包含 scope 等 Token 授权参数。
 */
public record MobileAuthenticationCommand(
        @Positive
        @NotNull
        Long challengeId,
        @NotBlank
        String code,
        String deviceId,
        String deviceType,
        String deviceName,
        @NotBlank
        String clientAppId,
        String clientPlatform,
        String clientVersion,
        String channelCode
) {
    public MobileAuthenticationCommand(
            Long challengeId,
            String code,
            String deviceId,
            String deviceType,
            String deviceName,
            String clientAppId,
            String clientPlatform,
            String clientVersion
    ) {
        this(challengeId, code, deviceId, deviceType, deviceName,
                clientAppId, clientPlatform, clientVersion, null);
    }
}
