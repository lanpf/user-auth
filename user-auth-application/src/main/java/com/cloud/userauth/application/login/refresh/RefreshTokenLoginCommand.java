package com.cloud.userauth.application.login.refresh;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenLoginCommand(@NotBlank String clientAppId, @NotBlank String refreshToken) {
}
