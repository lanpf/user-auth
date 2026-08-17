package com.cloud.userauth.api.authentication;

import com.cloud.framework.core.Request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BindExternalCredentialApiCommand(
        @NotNull Long authenticatedUserId,
        @NotNull Long authenticatedAuthAccountId,
        @NotBlank String issuer,
        @NotBlank String authorizationCode
) implements Request {
}
