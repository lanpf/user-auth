package com.cloud.userauth.application.login.external;

import java.time.Instant;

public record ExternalLoginAttemptCommandOutput(
        String loginAttemptId,
        boolean mobileVerificationRequired,
        Instant expiresAt
) {
}
