package com.cloud.userauth.application.login.external;

public record ExternalLoginOutput(
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
