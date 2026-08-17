package com.cloud.userauth.api.authentication;

public record ExchangeH5SessionHandoffApiCommandOutput(
        String sessionCredential,
        long expiresIn,
        String handoffId
) {
}
