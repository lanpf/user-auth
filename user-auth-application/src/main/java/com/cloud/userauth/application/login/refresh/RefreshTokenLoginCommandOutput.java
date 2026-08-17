package com.cloud.userauth.application.login.refresh;

public record RefreshTokenLoginCommandOutput(
        String tokenType,
        String accessToken,
        String refreshToken,
        Long expiresIn,
        String scope,
        Long userId,
        Long authAccountId,
        String sessionId
) {
}
