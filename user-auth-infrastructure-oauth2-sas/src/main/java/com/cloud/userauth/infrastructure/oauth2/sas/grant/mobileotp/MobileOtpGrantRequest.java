package com.cloud.userauth.infrastructure.oauth2.sas.grant.mobileotp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * mobile_otp Grant 的类型化协议请求。
 *
 * <p>{@code scope} 属于 Token 授权，不进入手机号身份认证流程。
 */
public record MobileOtpGrantRequest(
        @Positive
        Long challengeId,
        @NotBlank
        String code,
        String scope,
        String deviceId,
        String deviceType,
        String deviceName,
        @NotBlank
        String clientAppId,
        String clientPlatform,
        String clientVersion,
        String channelCode
) {
    public MobileOtpGrantRequest(
            Long challengeId,
            String code,
            String scope,
            String deviceId,
            String deviceType,
            String deviceName,
            String clientAppId,
            String clientPlatform,
            String clientVersion
    ) {
        this(challengeId, code, scope, deviceId, deviceType, deviceName,
                clientAppId, clientPlatform, clientVersion, null);
    }
}
