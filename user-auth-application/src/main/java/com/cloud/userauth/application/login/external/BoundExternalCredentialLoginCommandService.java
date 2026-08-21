package com.cloud.userauth.application.login.external;

import com.cloud.userauth.application.common.ApplicationError;
import com.cloud.userauth.application.common.ApplicationException;
import com.cloud.userauth.application.port.ClientRenewalPolicy;
import com.cloud.userauth.application.port.ClientRenewalPolicyResolver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

/** 已绑定外部 Credential 的无状态授权码登录/续期。 */
@Validated
@RequiredArgsConstructor
public class BoundExternalCredentialLoginCommandService {
    private final ExternalAttemptLoginCommandService externalAttemptLoginCommandService;
    private final ExternalLoginCommandService externalLoginCommandService;
    private final ClientRenewalPolicyResolver renewalPolicyResolver;

    public ExternalLoginOutput execute(
            @Valid BoundExternalCredentialLoginCommand command
    ) {
        if (renewalPolicyResolver.resolve(command.clientAppId())
                != ClientRenewalPolicy.EXTERNAL_AUTHORIZATION_CODE) {
            throw new ApplicationException(ApplicationError.APP_LOGIN_CLIENT_NOT_ALLOWED);
        }
        ExternalAttemptLoginOutput attempt = externalAttemptLoginCommandService.execute(
                ExternalAttemptLoginCommand.withAuthorizationCode(command.issuer(), command.authorizationCode())
        );
        if (attempt.mobileVerificationRequired()) {
            throw new ApplicationException(ApplicationError.APP_LOGIN_REJECTED);
        }
        return externalLoginCommandService.execute(new ExternalLoginCommand(
                attempt.loginAttemptId(), null, null, command.deviceId(), command.deviceType(),
                command.deviceName(), command.clientAppId(), command.clientPlatform(),
                command.clientVersion(), command.channelCode()));
    }
}
