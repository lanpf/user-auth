package com.cloud.userauth.infrastructure.oauth2.redis;

import java.time.Instant;
import java.util.Set;

record OAuth2AuthorizationRedisData(
        String id,
        String registeredClientId,
        String principalName,
        String authorizationGrantType,
        Set<String> authorizedScopes,
        String attributes,
        String state,
        AccessTokenData accessToken,
        RefreshTokenData refreshToken
) {
    OAuth2AuthorizationRedisData {
        authorizedScopes = authorizedScopes == null ? Set.of() : Set.copyOf(authorizedScopes);
    }

    record AccessTokenData(
            String value,
            Instant issuedAt,
            Instant expiresAt,
            String metadata,
            String tokenType,
            Set<String> scopes
    ) {
        AccessTokenData {
            scopes = scopes == null ? Set.of() : Set.copyOf(scopes);
        }
    }

    record RefreshTokenData(
            String value,
            Instant issuedAt,
            Instant expiresAt,
            String metadata
    ) {
    }
}
