package com.cloud.userauth.application.login.external;

import com.cloud.userauth.application.common.ApplicationError;
import com.cloud.userauth.application.common.ApplicationException;
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
 * 网关已验证合作方可信手机号断言直连登录编排。
 */
@Validated
@RequiredArgsConstructor
public class TrustedPartnerMobileLoginCommandService {
    private final ExternalAttemptLoginCommandService externalAttemptLoginCommandService;
    private final LoginTokenIssuer loginTokenIssuer;

    public ExternalLoginOutput execute(@Valid TrustedPartnerMobileLoginCommand command) {
        ExternalAttemptLoginOutput attempt = externalAttemptLoginCommandService.acceptIdentity(
                new ExternalIdentity(
                        new CredentialIssuer(command.partnerCode(), CredentialIssuerType.TRUSTED_PARTNER),
                        new Principal(command.partnerBizId()),
                        new LoginMobile(command.mobile()),
                        true));
        if (attempt.mobileVerificationRequired()) {
            throw new ApplicationException(ApplicationError.APP_LOGIN_REJECTED);
        }
        return loginTokenIssuer.issueExternalLogin(
                new ExternalLoginCommand(
                        attempt.loginAttemptId(),
                        null,
                        null,
                        command.deviceId(),
                        command.deviceType(),
                        command.deviceName(),
                        command.clientAppId(),
                        command.clientPlatform(),
                        command.clientVersion(),
                        command.channelCode()),
                ExternalCredentialBinding.DO_NOT_BIND);
    }
}
