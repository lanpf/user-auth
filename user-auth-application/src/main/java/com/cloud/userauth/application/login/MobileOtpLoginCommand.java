package com.cloud.userauth.application.login;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MobileOtpLoginCommand(
        @NotNull @Positive Long challengeId,
        @NotBlank String code,
        String deviceId,
        String deviceType,
        String deviceName,
        @NotBlank String clientAppId,
        String clientPlatform,
        String clientVersion,
        String channelCode
) {
}
