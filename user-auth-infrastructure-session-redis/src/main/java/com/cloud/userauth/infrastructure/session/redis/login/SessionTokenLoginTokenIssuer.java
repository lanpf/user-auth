package com.cloud.userauth.infrastructure.session.redis.login;

import com.cloud.userauth.application.authentication.AuthenticatedSession;
import com.cloud.userauth.application.login.MobileAuthenticationCommand;
import com.cloud.userauth.application.login.MobileAuthenticationCommandOutput;
import com.cloud.userauth.application.login.MobileOtpAuthenticationProcess;
import com.cloud.userauth.application.login.MobileOtpLoginCommand;
import com.cloud.userauth.application.login.MobileOtpLoginCommandOutput;
import com.cloud.userauth.application.login.external.ExternalAuthenticationCommand;
import com.cloud.userauth.application.login.external.ExternalAuthenticationCommandOutput;
import com.cloud.userauth.application.login.external.ExternalAuthenticationProcess;
import com.cloud.userauth.application.login.external.ExternalCredentialBinding;
import com.cloud.userauth.application.login.external.ExternalLoginCommand;
import com.cloud.userauth.application.login.external.ExternalLoginCommandOutput;
import com.cloud.userauth.application.port.SessionTokenStore;
import com.cloud.userauth.application.port.LoginTokenIssuer;
import com.cloud.userauth.domain.authentication.session.SessionId;
import java.time.Duration;
import lombok.RequiredArgsConstructor;

/** 认证完成后创建不透明 Session Token 的登录结果交付器。 */
@RequiredArgsConstructor
public final class SessionTokenLoginTokenIssuer implements LoginTokenIssuer {
    private static final String TOKEN_TYPE = "SESSION_TOKEN";

    private final MobileOtpAuthenticationProcess mobileAuthenticationProcess;
    private final ExternalAuthenticationProcess externalAuthenticationProcess;
    private final SessionTokenStore sessionTokenStore;
    private final Duration tokenTtl;

    @Override
    public MobileOtpLoginCommandOutput issueMobileOtpLogin(MobileOtpLoginCommand command) {
        MobileAuthenticationCommandOutput authenticated = mobileAuthenticationProcess.authenticate(
                new MobileAuthenticationCommand(command.challengeId(), command.code(), command.deviceId(),
                        command.deviceType(), command.deviceName(), command.clientAppId(),
                        command.clientPlatform(), command.clientVersion(), command.channelCode()));
        SessionTokenStore.IssuedSessionToken sessionToken = sessionTokenStore.create(
                new AuthenticatedSession(authenticated.userId(), authenticated.authAccountId(),
                        new SessionId(authenticated.sessionId())), tokenTtl);
        return new MobileOtpLoginCommandOutput(TOKEN_TYPE, sessionToken.token(), null,
                sessionToken.ttl().toSeconds(), null, authenticated.userId(), authenticated.authAccountId(),
                authenticated.sessionId(), authenticated.fromRegistrationFlow(), authenticated.replayed());
    }

    @Override
    public ExternalLoginCommandOutput issueExternalLogin(
            ExternalLoginCommand command,
            ExternalCredentialBinding credentialBinding
    ) {
        ExternalAuthenticationCommandOutput authenticated = externalAuthenticationProcess.authenticate(
                new ExternalAuthenticationCommand(command.loginAttemptId(), command.challengeId(), command.code(),
                        command.deviceId(), command.deviceType(), command.deviceName(), command.clientAppId(),
                        command.clientPlatform(), command.clientVersion(),
                        command.channelCode(),
                        credentialBinding == ExternalCredentialBinding.BIND));
        SessionTokenStore.IssuedSessionToken sessionToken = sessionTokenStore.create(
                new AuthenticatedSession(authenticated.userId(), authenticated.authAccountId(),
                        new SessionId(authenticated.sessionId())), tokenTtl);
        return new ExternalLoginCommandOutput(TOKEN_TYPE, sessionToken.token(), null,
                sessionToken.ttl().toSeconds(),
                null, authenticated.userId(), authenticated.authAccountId(), authenticated.sessionId());
    }
}
