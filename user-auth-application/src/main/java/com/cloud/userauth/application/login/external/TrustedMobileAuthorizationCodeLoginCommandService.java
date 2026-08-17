package com.cloud.userauth.application.login.external;

import com.cloud.userauth.application.port.LoginTokenIssuer;
import com.cloud.userauth.domain.authentication.credential.CredentialIssuer;
import com.cloud.userauth.domain.authentication.credential.CredentialIssuerType;
import com.cloud.userauth.domain.authentication.credential.LoginMobile;
import com.cloud.userauth.domain.authentication.credential.Principal;
import com.cloud.userauth.domain.authentication.external.ExternalIdentity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

/**
 * 合作方已在网关完成验签、时效和重放校验后的直连登录编排。
 */
@Validated
@RequiredArgsConstructor
public class TrustedMobileAuthorizationCodeLoginCommandService {
    private final ExternalLoginAttemptCommandService externalLoginAttemptCommandService;
    private final LoginTokenIssuer loginTokenIssuer;

    public ExternalLoginCommandOutput execute(@Valid TrustedMobileAuthorizationCodeLoginCommand command) {
        ExternalLoginAttemptCommandOutput preLogin = externalLoginAttemptCommandService.acceptTrustedIdentity(
                new ExternalIdentity(
                        new CredentialIssuer(command.issuer(), CredentialIssuerType.TRUSTED_PARTNER),
                        new Principal(command.authorizationCode()),
                        new LoginMobile(command.mobile()),
                        true,
                        null));
        return loginTokenIssuer.issueExternalLogin(new ExternalLoginCommand(
                preLogin.loginAttemptId(),
                null,
                null,
                command.deviceId(),
                command.deviceType(),
                command.deviceName(),
                command.clientAppId(),
                command.clientPlatform(),
                command.clientVersion(),
                command.channelCode()), ExternalCredentialBinding.DO_NOT_BIND);
    }
}
