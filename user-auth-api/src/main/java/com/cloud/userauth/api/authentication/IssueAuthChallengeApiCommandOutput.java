package com.cloud.userauth.api.authentication;

import java.io.Serializable;
import java.time.Instant;

public record IssueAuthChallengeApiCommandOutput(
        Long challengeId,
        Instant expiresAt,
        boolean reused
) implements Serializable {
}
