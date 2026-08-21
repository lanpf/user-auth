package com.cloud.userauth.api.authentication;

import java.io.Serializable;
import java.time.Instant;

public record ExternalAttemptLoginApiCommandOutput(
        String loginAttemptId,
        String mobile,
        Boolean mobileVerificationRequired,
        Instant expiresAt
) implements Serializable {
}
