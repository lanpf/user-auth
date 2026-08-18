package com.cloud.userauth.api.authentication;

import com.cloud.framework.core.Request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LogoutApiCommand(
        @NotNull Long authenticatedUserId,
        @NotBlank String sessionId
) implements Request {
}
