package com.cloud.userauth.infrastructure.oauth2.sas.login;

import com.cloud.userauth.application.login.MobileOtpLoginCommand;
import com.cloud.userauth.application.login.MobileOtpLoginCommandOutput;
import com.cloud.userauth.application.login.external.ExternalLoginCommand;
import com.cloud.userauth.application.login.external.ExternalLoginCommandOutput;
import com.cloud.userauth.application.login.external.ExternalCredentialBinding;
import com.cloud.userauth.application.port.LoginTokenIssuer;
import com.cloud.userauth.infrastructure.oauth2.sas.grant.external.ExternalIdentityGrantRequest;
import com.cloud.userauth.infrastructure.oauth2.sas.grant.external.mapper.ExternalIdentityGrantRequestMapper;
import com.cloud.userauth.infrastructure.oauth2.sas.grant.mobileotp.MobileOtpGrantRequest;
import com.cloud.userauth.infrastructure.oauth2.sas.grant.mobileotp.mapper.MobileOtpGrantRequestMapper;
import com.cloud.userauth.infrastructure.oauth2.sas.login.mapper.SasTokenEndpointPayloadMapper;
import com.cloud.userauth.infrastructure.oauth2.sas.login.mapper.SasExternalTokenEndpointPayloadMapper;
import com.cloud.userauth.infrastructure.oauth2.sas.login.tokenendpoint.SasTokenEndpointClient;
import com.cloud.userauth.infrastructure.oauth2.sas.login.tokenendpoint.SasTokenEndpointPayload;
import com.cloud.userauth.infrastructure.oauth2.sas.scope.SasClientScopeResolver;
import lombok.RequiredArgsConstructor;

/** 通过本机 SAS 标准 Token Endpoint 完成手机号登录。 */
@RequiredArgsConstructor
public final class SasLoginTokenIssuer implements LoginTokenIssuer {
    private final SasClientScopeResolver clientScopeResolver;
    private final SasTokenEndpointClient tokenEndpointClient;
    private final MobileOtpGrantRequestMapper requestMapper;
    private final SasTokenEndpointPayloadMapper responseMapper;
    private final ExternalIdentityGrantRequestMapper externalRequestMapper;
    private final SasExternalTokenEndpointPayloadMapper externalResponseMapper;

    @Override
    public MobileOtpLoginCommandOutput issueMobileOtpLogin(MobileOtpLoginCommand command) {
        String scope = String.join(" ", clientScopeResolver.resolve(command.clientAppId()));
        MobileOtpGrantRequest grantRequest = requestMapper.toGrantRequest(command, scope);
        SasTokenEndpointPayload response = tokenEndpointClient.requestToken(grantRequest);
        return responseMapper.toOutput(response);
    }

    @Override
    public ExternalLoginCommandOutput issueExternalLogin(
            ExternalLoginCommand command,
            ExternalCredentialBinding credentialBinding
    ) {
        String scope = String.join(" ", clientScopeResolver.resolve(command.clientAppId()));
        ExternalIdentityGrantRequest grantRequest = externalRequestMapper.toGrantRequest(
                command, scope, credentialBinding);
        SasTokenEndpointPayload response = tokenEndpointClient.requestExternalToken(grantRequest);
        return externalResponseMapper.toOutput(response);
    }
}
