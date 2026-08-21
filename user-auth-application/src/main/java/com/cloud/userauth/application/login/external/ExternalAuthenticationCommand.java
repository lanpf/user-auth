package com.cloud.userauth.application.login.external;

public record ExternalAuthenticationCommand(
        String loginAttemptId,
        Long challengeId,
        String code,
        String deviceId,
        String deviceType,
        String deviceName,
        String clientAppId,
        String clientPlatform,
        String clientVersion,
        String channelCode,
        boolean bindExternalIdentity
) {
}
