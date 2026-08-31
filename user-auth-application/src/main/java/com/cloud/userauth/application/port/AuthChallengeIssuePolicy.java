package com.cloud.userauth.application.port;

import java.time.Duration;

public record AuthChallengeIssuePolicy(Duration ttl, Duration reuseWindow) {
}
