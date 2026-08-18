package com.cloud.userauth.infrastructure.oauth2.sas.logout;

import com.cloud.userauth.application.port.LoginSessionRevoker;
import com.cloud.userauth.domain.authentication.session.SessionId;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;

/** 通过 SAS 授权存储撤销一个登录会话关联的所有 OAuth2 Token。 */
@RequiredArgsConstructor
public final class SasLoginSessionRevoker implements LoginSessionRevoker {
    private final OAuth2AuthorizationService authorizationService;

    @Override
    public void revoke(SessionId sessionId) {
        OAuth2Authorization authorization = authorizationService.findById(sessionId.value());
        if (authorization == null) {
            return;
        }
        OAuth2Authorization.Builder builder = OAuth2Authorization.from(authorization);
        invalidate(builder, authorization.getAccessToken());
        invalidate(builder, authorization.getRefreshToken());
        authorizationService.save(builder.build());
    }

    private static void invalidate(
            OAuth2Authorization.Builder builder,
            OAuth2Authorization.Token<? extends OAuth2Token> token
    ) {
        if (token != null) {
            builder.invalidate(token.getToken());
        }
    }
}
