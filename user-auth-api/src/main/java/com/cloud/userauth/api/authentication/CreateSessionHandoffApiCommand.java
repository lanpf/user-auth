package com.cloud.userauth.api.authentication;

import com.cloud.framework.core.Request;
import com.cloud.userauth.api.enums.SessionHandoffTargetApiEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateSessionHandoffApiCommand(
        @NotNull @Positive Long userId,
        @NotBlank String sessionId,
        @NotNull SessionHandoffTargetApiEnum target
) implements Request {
}
