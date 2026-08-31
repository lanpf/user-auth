package com.cloud.userauth.application.challenge;

import com.cloud.userauth.domain.authentication.challenge.AuthChallengeScene;
import com.cloud.userauth.domain.authentication.challenge.AuthChallengeType;

public record IssueAuthChallengeCommand(
        AuthChallengeType challengeType,
        AuthChallengeScene scene,
        String target
        ) {
}
