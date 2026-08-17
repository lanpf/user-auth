package com.cloud.userauth.api.authorization;

public record ReconcileChannelAuthorizationPolicyApiCommandOutput(
        int processedUserCount,
        Long policyVersion,
        boolean hasPendingUsers
) {
}
