package com.cloud.userauth.application.authorization;

public record ReconcileChannelAuthorizationPolicyCommand(String channelCode, int batchSize) {
}
