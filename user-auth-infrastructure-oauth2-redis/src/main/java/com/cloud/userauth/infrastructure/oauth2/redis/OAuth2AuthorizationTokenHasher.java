package com.cloud.userauth.infrastructure.oauth2.redis;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;

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

    OAuth2Authorization restoreMatchedToken(
            OAuth2Authorization authorization,
            String rawToken,
            OAuth2TokenType tokenType
    ) {
        OAuth2Authorization.Builder builder = OAuth2Authorization.from(authorization);
        if (OAuth2TokenType.ACCESS_TOKEN.equals(tokenType)) {
            restore(builder, authorization.getAccessToken(), rawToken);
        } else if (OAuth2TokenType.REFRESH_TOKEN.equals(tokenType)) {
            restore(builder, authorization.getRefreshToken(), rawToken);
        }
        return builder.build();
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

    private <T extends OAuth2Token> void restore(
            OAuth2Authorization.Builder builder,
            OAuth2Authorization.Token<T> tokenHolder,
            String rawToken
    ) {
        if (tokenHolder == null) {
            return;
        }
        OAuth2Token token = tokenHolder.getToken();
        OAuth2Token replacement;
        if (token instanceof OAuth2AccessToken accessToken) {
            replacement = new OAuth2AccessToken(
                    accessToken.getTokenType(),
                    rawToken,
                    accessToken.getIssuedAt(),
                    accessToken.getExpiresAt(),
                    accessToken.getScopes());
        } else if (token instanceof OAuth2RefreshToken) {
            replacement = new OAuth2RefreshToken(
                    rawToken, token.getIssuedAt(), token.getExpiresAt());
        } else {
            return;
        }
        builder.token(replacement, metadata -> {
            metadata.remove(HASHED_METADATA);
            tokenHolder.getMetadata().forEach((name, value) -> {
                if (!HASHED_METADATA.equals(name)) {
                    metadata.put(name, value);
                }
            });
        });
    }
}
