package com.cloud.userauth.application.authorization;

public record ChangeChannelAuthorizationPolicyStatusCommand(
        String channelCode,
        Long expectedVersion
) {
}
