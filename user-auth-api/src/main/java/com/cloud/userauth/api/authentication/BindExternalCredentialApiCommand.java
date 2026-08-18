package com.cloud.userauth.api.authentication;

import com.cloud.framework.core.Request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BindExternalCredentialApiCommand(
        @NotNull Long authenticatedUserId,
        @NotBlank String authenticatedSessionId,
        @NotBlank String issuer,
        @NotBlank String authorizationCode
) implements Request {
}
