package com.cloud.userauth.application.login.external;

import java.time.Instant;

public record ExternalLoginAttemptOutput(
        String loginAttemptId,
        boolean mobileVerificationRequired,
        Instant expiresAt
) {
}
