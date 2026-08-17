package com.cloud.userauth.api.authentication;

import java.io.Serializable;

public record ExternalLoginApiCommandOutput(
        String tokenType,
        String accessToken,
        String refreshToken,
        Long expiresIn,
        String scope,
        Long userId,
        Long authAccountId,
        String sessionId
) implements Serializable {
}
