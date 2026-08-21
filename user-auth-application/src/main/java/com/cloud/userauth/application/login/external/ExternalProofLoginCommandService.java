package com.cloud.userauth.application.login.external;

import com.cloud.userauth.application.common.ApplicationError;
import com.cloud.userauth.application.common.ApplicationException;
import com.cloud.userauth.application.port.LoginTokenIssuer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Validated
@RequiredArgsConstructor
public class ExternalProofLoginCommandService {
    private final ExternalAttemptLoginCommandService externalAttemptLoginCommandService;
    private final LoginTokenIssuer loginTokenIssuer;

    public ExternalLoginOutput execute(@Valid ExternalProofLoginCommand command) {
        ExternalAttemptLoginOutput attempt = externalAttemptLoginCommandService.execute(
                new ExternalAttemptLoginCommand(
                        command.issuer(), command.proofType(), command.proofParameters()));
        if (attempt.mobile() == null || attempt.mobileVerificationRequired()) {
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
                ExternalCredentialBinding.BIND);
    }
}
