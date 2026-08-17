package com.cloud.userauth.api.authentication;

import com.cloud.framework.core.Request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateH5SessionHandoffApiCommand(
        @NotNull @Positive Long userId,
        @NotNull @Positive Long authAccountId,
        @NotBlank String sessionId
) implements Request {
}
