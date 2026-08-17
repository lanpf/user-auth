package com.cloud.userauth.api.authentication;

import java.io.Serializable;
import java.time.Instant;

public record ExternalLoginAttemptApiCommandOutput(
        String loginAttemptId,
        Boolean mobileVerificationRequired,
        Instant expiresAt
) implements Serializable {
}
