package com.cloud.userauth.infrastructure.oauth2.sas.grant.external;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record ExternalIdentityGrantRequest(
        @NotBlank String loginAttemptId,
        @Positive Long challengeId,
        String code,
        String scope,
        String deviceId,
        String deviceType,
        String deviceName,
        @NotBlank String clientAppId,
        String clientPlatform,
        String clientVersion,
        String channelCode,
        boolean bindExternalIdentity
) {
    public ExternalIdentityGrantRequest(
            String loginAttemptId, Long challengeId, String code, String scope,
            String deviceId, String deviceType, String deviceName, String clientAppId,
            String clientPlatform, String clientVersion
    ) {
        this(loginAttemptId, challengeId, code, scope, deviceId, deviceType, deviceName,
                clientAppId, clientPlatform, clientVersion, null, true);
    }
}
