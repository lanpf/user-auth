package com.cloud.userauth.infrastructure.oauth2.sas.login.tokenendpoint;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SasTokenEndpointPayload(
        @NotBlank
        String tokenType,
        @NotBlank
        String accessToken,
        String refreshToken,
        @NotNull
        @Positive
        Long expiresIn,
        String scope,
        @NotNull
        Long userId,
        @NotNull
        Long authAccountId,
        @NotBlank
        String sessionId,
        @NotNull
        Boolean fromRegistrationFlow,
        @NotNull
        Boolean replayed
) {
}
