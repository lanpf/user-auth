package com.cloud.userauth.infrastructure.oauth2.sas.login.tokenendpoint;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SasRefreshTokenEndpointPayload(
        @NotBlank String tokenType,
        @NotBlank String accessToken,
        @NotBlank String refreshToken,
        @NotNull @Positive Long expiresIn,
        String scope
) {
}
