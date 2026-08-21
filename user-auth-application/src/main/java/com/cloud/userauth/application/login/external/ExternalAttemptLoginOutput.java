package com.cloud.userauth.application.login.external;

import java.time.Instant;

public record ExternalAttemptLoginOutput(
        String loginAttemptId,
        String mobile,
        boolean mobileVerificationRequired,
        Instant expiresAt
) {
}
