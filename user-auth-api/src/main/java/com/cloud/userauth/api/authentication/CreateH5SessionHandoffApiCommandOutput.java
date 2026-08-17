package com.cloud.userauth.api.authentication;

public record CreateH5SessionHandoffApiCommandOutput(
        String handoffId,
        String ticket
) {
}
