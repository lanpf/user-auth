package com.cloud.userauth.application.login;

public record MobileOtpLoginCommand(
        Long challengeId,
        String code,
        String deviceId,
        String deviceType,
        String deviceName,
        String clientAppId,
        String clientPlatform,
        String clientVersion,
        String channelCode
) {
}
