package com.cloud.userauth.api.authentication;

public record CreateSessionHandoffApiCommandOutput(
        String handoffId,
        String ticket
) {
}
