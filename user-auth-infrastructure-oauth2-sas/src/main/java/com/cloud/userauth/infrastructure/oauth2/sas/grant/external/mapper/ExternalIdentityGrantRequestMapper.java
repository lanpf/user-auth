package com.cloud.userauth.infrastructure.oauth2.sas.grant.external.mapper;

import com.cloud.userauth.application.login.external.ExternalAuthenticationCommand;
import com.cloud.userauth.application.login.external.ExternalLoginCommand;
import com.cloud.userauth.application.login.external.ExternalCredentialBinding;
import com.cloud.userauth.infrastructure.oauth2.sas.grant.external.ExternalIdentityGrantRequest;

public interface ExternalIdentityGrantRequestMapper {
    ExternalIdentityGrantRequest toGrantRequest(
            ExternalLoginCommand command,
            String scope,
            ExternalCredentialBinding credentialBinding
    );

    ExternalAuthenticationCommand toAuthenticationCommand(ExternalIdentityGrantRequest request);
}
