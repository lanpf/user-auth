package com.cloud.userauth.application.port;

public interface AuthChallengePolicyProvider {
    AuthChallengeIssuePolicy currentPolicy();
}
