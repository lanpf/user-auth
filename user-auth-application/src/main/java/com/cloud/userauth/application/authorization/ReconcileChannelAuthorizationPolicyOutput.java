package com.cloud.userauth.application.authorization;

public record ReconcileChannelAuthorizationPolicyOutput(
        int processedUserCount,
        Long policyVersion,
        boolean hasPendingUsers
) {
}
