package com.cloud.userauth.api.authentication;

import com.cloud.framework.core.Request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record ExternalLoginApiCommand(
        @NotBlank String loginAttemptId,
        @Positive Long challengeId,
        String code,
        @NotBlank String clientAppId,
        String clientPlatform,
        String clientVersion,
        @NotBlank String channelCode,
        String deviceId,
        String deviceType,
        String deviceName
) implements Request {
}
