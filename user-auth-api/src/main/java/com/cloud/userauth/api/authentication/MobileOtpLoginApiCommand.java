package com.cloud.userauth.api.authentication;

import com.cloud.framework.core.Request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MobileOtpLoginApiCommand(
        @NotNull @Positive Long challengeId,
        @NotBlank String code,
        @NotBlank String clientAppId,
        String clientPlatform,
        String clientVersion,
        @NotBlank String channelCode,
        String deviceId,
        String deviceType,
        String deviceName
) implements Request {
}
