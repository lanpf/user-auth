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
    public ExternalAuthenticationCommand(
            String loginAttemptId, Long challengeId, String code, String deviceId,
            String deviceType, String deviceName, String clientAppId,
            String clientPlatform, String clientVersion
    ) {
        this(loginAttemptId, challengeId, code, deviceId, deviceType, deviceName,
                clientAppId, clientPlatform, clientVersion, null, true);
    }

    public ExternalAuthenticationCommand(
            String loginAttemptId, Long challengeId, String code, String deviceId,
            String deviceType, String deviceName, String clientAppId,
            String clientPlatform, String clientVersion, boolean bindExternalIdentity
    ) {
        this(loginAttemptId, challengeId, code, deviceId, deviceType, deviceName,
                clientAppId, clientPlatform, clientVersion, null, bindExternalIdentity);
    }
}
