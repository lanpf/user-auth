package com.cloud.userauth.infrastructure.challenge;

import com.cloud.userauth.application.port.AuthChallengeIssuePolicy;
import com.cloud.userauth.application.port.AuthChallengePolicyProvider;
import com.cloud.userauth.infrastructure.config.AuthChallengeProperties;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PropertiesAuthChallengePolicyProvider implements AuthChallengePolicyProvider {
    private final AuthChallengeProperties properties;

    @Override
    public AuthChallengeIssuePolicy currentPolicy() {
        AuthChallengeProperties.IssuePolicyProperties issuePolicy = properties.getIssuePolicy();
        return new AuthChallengeIssuePolicy(issuePolicy.getTtl(), issuePolicy.getReuseWindow());
    }
}
