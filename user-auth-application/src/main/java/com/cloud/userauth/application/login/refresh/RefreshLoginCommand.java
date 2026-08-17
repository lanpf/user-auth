package com.cloud.userauth.application.login.refresh;

import jakarta.validation.constraints.NotBlank;

public record RefreshLoginCommand(@NotBlank String clientAppId, @NotBlank String refreshToken) {
}
