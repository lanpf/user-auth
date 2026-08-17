package com.cloud.userauth.application.login.external;

public record ExternalAuthenticationCommandOutput(
        Long userId,
        Long authAccountId,
        String sessionId,
        boolean replayed
) {
}
