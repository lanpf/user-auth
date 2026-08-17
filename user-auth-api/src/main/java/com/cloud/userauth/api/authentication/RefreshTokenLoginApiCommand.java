package com.cloud.userauth.api.authentication;

import com.cloud.framework.core.Request;
import jakarta.validation.constraints.NotBlank;

public record RefreshTokenLoginApiCommand(
        @NotBlank String refreshToken,
        @NotBlank String clientAppId,
        String clientPlatform,
        String clientVersion
) implements Request {
}
