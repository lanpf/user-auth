package com.cloud.userauth.application.login.external;

import com.cloud.userauth.application.port.LoginTokenIssuer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Validated
@RequiredArgsConstructor
public class ExternalLoginCommandService {
    private final LoginTokenIssuer loginTokenIssuer;

    public ExternalLoginOutput execute(@Valid ExternalLoginCommand command) {
        return loginTokenIssuer.issueExternalLogin(command, ExternalCredentialBinding.BIND);
    }
}
