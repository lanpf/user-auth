package com.cloud.userauth.application.challenge;

import java.time.Instant;

public record IssueAuthChallengeCommandOutput(Long challengeId, Instant expiresAt, boolean reused) {
}
