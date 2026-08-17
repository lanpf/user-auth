package com.cloud.userauth.infrastructure.oauth2.redis;

import com.cloud.userauth.domain.authentication.session.LoginSession;
import com.cloud.userauth.domain.authentication.session.LoginSessionRepository;
import com.cloud.userauth.domain.authentication.session.SessionId;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;

@RequiredArgsConstructor
public class RedisOAuth2AuthorizationService implements OAuth2AuthorizationService {
    private static final OAuth2AuthorizationTokenHasher TOKEN_HASHER =
            new OAuth2AuthorizationTokenHasher();

    private final OAuth2AuthorizationRedisStore store;
    private final LoginSessionRepository sessionRepository;
    private final Clock clock;

    @Override
    public void save(OAuth2Authorization authorization) {
        OAuth2Authorization sanitized = TOKEN_HASHER.sanitize(authorization);
        store.save(sanitized);
        synchronizeSessionRevocation(sanitized);
    }

    @Override
    public void remove(OAuth2Authorization authorization) {
        store.remove(authorization);
    }

    @Override
    public OAuth2Authorization findById(String id) {
        return store.findById(id);
    }

    @Override
    public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
        String tokenHash = TOKEN_HASHER.hash(token);
        if (tokenType == null) {
            OAuth2Authorization authorization =
                    store.findByTokenHash(tokenHash, OAuth2TokenType.ACCESS_TOKEN);
            if (authorization != null) {
                return authorization;
            }
            authorization = store.findByTokenHash(tokenHash, OAuth2TokenType.REFRESH_TOKEN);
            if (authorization == null) {
                revokeReusedRefreshTokenFamily(tokenHash);
            }
            return authorization;
        }
        if (!isBearerToken(tokenType)) {
            return null;
        }
        OAuth2Authorization authorization = store.findByTokenHash(tokenHash, tokenType);
        if (authorization == null && OAuth2TokenType.REFRESH_TOKEN.equals(tokenType)) {
            revokeReusedRefreshTokenFamily(tokenHash);
        }
        return authorization;
    }

    private void revokeReusedRefreshTokenFamily(String refreshTokenHash) {
        store.findReusedRefreshTokenAuthorizationId(refreshTokenHash)
                .ifPresent(this::revokeAuthorizationFamily);
    }

    private void revokeAuthorizationFamily(String authorizationId) {
        OAuth2Authorization authorization = store.findById(authorizationId);
        if (authorization == null) {
            return;
        }
        OAuth2Authorization.Builder builder = OAuth2Authorization.from(authorization);
        invalidate(builder, authorization.getAccessToken());
        invalidate(builder, authorization.getRefreshToken());
        store.save(builder.build());
        revokeSession(authorizationId);
    }

    private void synchronizeSessionRevocation(OAuth2Authorization authorization) {
        OAuth2Authorization.Token<OAuth2RefreshToken> refreshToken = authorization.getRefreshToken();
        if (refreshToken != null && refreshToken.isInvalidated()) {
            revokeSession(authorization.getId());
        }
    }

    private void revokeSession(String authorizationId) {
        if (authorizationId == null) {
            return;
        }
        sessionRepository.findById(new SessionId(authorizationId))
                .ifPresent(this::revokeSession);
    }

    private void revokeSession(LoginSession session) {
        session.revoke(clock.instant());
        sessionRepository.save(session);
    }

    private static void invalidate(
            OAuth2Authorization.Builder builder,
            OAuth2Authorization.Token<? extends OAuth2Token> token
    ) {
        if (token != null) {
            builder.invalidate(token.getToken());
        }
    }

    private static boolean isBearerToken(OAuth2TokenType tokenType) {
        return OAuth2TokenType.ACCESS_TOKEN.equals(tokenType)
                || OAuth2TokenType.REFRESH_TOKEN.equals(tokenType);
    }
}
