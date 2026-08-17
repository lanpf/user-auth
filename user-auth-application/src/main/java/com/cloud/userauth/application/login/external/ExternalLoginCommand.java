package com.cloud.userauth.application.login.external;

import jakarta.validation.constraints.NotBlank;

public record ExternalLoginCommand(
        @NotBlank String loginAttemptId,
        Long challengeId,
        String code,
        String deviceId,
        String deviceType,
        String deviceName,
        @NotBlank String clientAppId,
        String clientPlatform,
        String clientVersion,
        String channelCode
) {
}
