package com.cloud.userauth.application.login;

public record MobileOtpLoginCommandOutput(
        String tokenType,
        String accessToken,
        String refreshToken,
        Long expiresIn,
        String scope,
        Long userId,
        Long authAccountId,
        String sessionId,
        Boolean fromRegistrationFlow,
        Boolean replayed
) {
}
