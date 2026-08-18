package com.cloud.userauth.application.challenge;

import java.time.Instant;

public record IssueAuthChallengeOutput(Long challengeId, Instant expiresAt, boolean reused) {
}
