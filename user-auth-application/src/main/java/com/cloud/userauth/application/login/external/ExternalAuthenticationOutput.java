package com.cloud.userauth.application.login.external;

public record ExternalAuthenticationOutput(
        Long userId,
        Long authAccountId,
        String sessionId,
        boolean replayed
) {
}
