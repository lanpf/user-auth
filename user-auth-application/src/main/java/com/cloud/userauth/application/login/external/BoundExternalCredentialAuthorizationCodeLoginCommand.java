package com.cloud.userauth.application.login.external;

import jakarta.validation.constraints.NotBlank;

public record BoundExternalCredentialAuthorizationCodeLoginCommand(
        @NotBlank String issuer,
        @NotBlank String authorizationCode,
        String deviceId,
        String deviceType,
        String deviceName,
        @NotBlank String clientAppId,
        String clientPlatform,
        String clientVersion,
        String channelCode
) {
}
