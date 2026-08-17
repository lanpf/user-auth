package com.cloud.userauth.application.credential;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BindExternalCredentialCommand(
        @NotNull Long authenticatedUserId,
        @NotNull Long authenticatedAuthAccountId,
        @NotBlank String issuer,
        @NotBlank String authorizationCode
) {
}
