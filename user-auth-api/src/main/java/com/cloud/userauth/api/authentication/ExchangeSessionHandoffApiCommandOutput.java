package com.cloud.userauth.api.authentication;

public record ExchangeSessionHandoffApiCommandOutput(
        String sessionCredential,
        long expiresIn,
        String handoffId
) {
}
