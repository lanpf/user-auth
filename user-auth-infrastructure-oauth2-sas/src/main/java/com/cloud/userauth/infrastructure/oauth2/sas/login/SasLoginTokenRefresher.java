package com.cloud.userauth.infrastructure.oauth2.sas.login;

import com.cloud.userauth.application.common.ApplicationError;
import com.cloud.userauth.application.common.ApplicationException;
import com.cloud.userauth.application.login.refresh.RefreshTokenLoginCommand;
import com.cloud.userauth.application.login.refresh.RefreshTokenLoginCommandOutput;
import com.cloud.userauth.application.port.LoginTokenRefresher;
import com.cloud.userauth.application.port.RefreshTokenRotationLock;
import com.cloud.userauth.domain.authentication.session.LoginSession;
import com.cloud.userauth.domain.authentication.session.LoginSessionRepository;
import com.cloud.userauth.domain.authentication.session.SessionId;
import com.cloud.userauth.domain.common.DomainException;
import com.cloud.userauth.infrastructure.oauth2.sas.login.tokenendpoint.SasRefreshTokenEndpointPayload;
import com.cloud.userauth.infrastructure.oauth2.sas.login.tokenendpoint.SasTokenEndpointClient;
import com.cloud.userauth.infrastructure.oauth2.sas.protocol.SasAuthorizationAttributes;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;

import java.time.Clock;
import java.util.Objects;

@RequiredArgsConstructor
public final class SasLoginTokenRefresher implements LoginTokenRefresher {
    private final OAuth2AuthorizationService authorizationService;
    private final SasTokenEndpointClient tokenEndpointClient;
    private final LoginSessionRepository loginSessionRepository;
    private final Clock clock;
    private final RefreshTokenRotationLock rotationLock;

    @Override
    public RefreshTokenLoginCommandOutput refresh(RefreshTokenLoginCommand command) {
        return rotationLock.execute(command.refreshToken(), () -> refreshLocked(command));
    }

    private RefreshTokenLoginCommandOutput refreshLocked(RefreshTokenLoginCommand command) {
        OAuth2Authorization authorization = authorizationService.findByToken(
                command.refreshToken(), OAuth2TokenType.REFRESH_TOKEN);
        if (authorization == null || !Objects.equals(
                command.clientAppId(),
                authorization.getAttribute(SasAuthorizationAttributes.CLIENT_APP_ID))) {
            throw new ApplicationException(ApplicationError.APP_REFRESH_TOKEN_FAILED);
        }
        ensureActiveLoginSession(command.clientAppId(), authorization);
        SasRefreshTokenEndpointPayload response =
                tokenEndpointClient.requestRefreshToken(command.refreshToken());
        return new RefreshTokenLoginCommandOutput(
                response.tokenType(),
                response.accessToken(),
                response.refreshToken(),
                response.expiresIn(),
                response.scope(),
                authorization.getAttribute(SasAuthorizationAttributes.USER_ID),
                authorization.getAttribute(SasAuthorizationAttributes.AUTH_ACCOUNT_ID),
                authorization.getAttribute(SasAuthorizationAttributes.SESSION_ID));
    }

    private void ensureActiveLoginSession(
            String clientAppId,
            OAuth2Authorization authorization
    ) {
        String sessionId = authorization.getAttribute(
                SasAuthorizationAttributes.SESSION_ID);
        LoginSession session = sessionId == null
                ? null
                : loginSessionRepository.findById(new SessionId(sessionId)).orElse(null);
        if (session == null || session.getClient() == null
                || !Objects.equals(clientAppId, session.getClient().appId())) {
            throw new ApplicationException(ApplicationError.APP_REFRESH_TOKEN_FAILED);
        }
        try {
            session.ensureActive(clock.instant());
        } catch (DomainException exception) {
            loginSessionRepository.save(session);
            throw new ApplicationException(ApplicationError.APP_REFRESH_TOKEN_FAILED, exception);
        }
    }
}
