package com.cloud.userauth.infrastructure.oauth2.redis;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;

final class OAuth2AuthorizationTokenHasher {
    private static final String HASHED_METADATA = "user_auth.token_value_hashed";

    OAuth2Authorization sanitize(OAuth2Authorization authorization) {
        OAuth2Authorization.Builder builder = OAuth2Authorization.from(authorization);
        replaceWithHash(builder, authorization.getAccessToken());
        replaceWithHash(builder, authorization.getRefreshToken());
        return builder.build();
    }

    String hash(String token) {
        return DigestUtils.sha256Hex(token);
    }

    private <T extends OAuth2Token> void replaceWithHash(
            OAuth2Authorization.Builder builder,
            OAuth2Authorization.Token<T> tokenHolder
    ) {
        if (tokenHolder == null
                || Boolean.TRUE.equals(tokenHolder.getMetadata().get(HASHED_METADATA))) {
            return;
        }
        OAuth2Token replacement = replacement(tokenHolder.getToken());
        builder.token(replacement, metadata -> {
            metadata.putAll(tokenHolder.getMetadata());
            metadata.put(HASHED_METADATA, true);
        });
    }

    private OAuth2Token replacement(OAuth2Token token) {
        String tokenHash = hash(token.getTokenValue());
        if (token instanceof OAuth2AccessToken accessToken) {
            return new OAuth2AccessToken(
                    accessToken.getTokenType(),
                    tokenHash,
                    accessToken.getIssuedAt(),
                    accessToken.getExpiresAt(),
                    accessToken.getScopes());
        }
        if (token instanceof OAuth2RefreshToken) {
            return new OAuth2RefreshToken(tokenHash, token.getIssuedAt(), token.getExpiresAt());
        }
        return token;
    }
}
