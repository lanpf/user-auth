package com.cloud.userauth.infrastructure.oauth2.sas.grant.external.mapper.mapstruct;

import com.cloud.framework.core.mapper.MapStructConfig;
import com.cloud.userauth.application.login.external.ExternalAuthenticationCommand;
import com.cloud.userauth.application.login.external.ExternalLoginCommand;
import com.cloud.userauth.application.login.external.ExternalCredentialBinding;
import com.cloud.userauth.infrastructure.oauth2.sas.grant.external.ExternalIdentityGrantRequest;
import com.cloud.userauth.infrastructure.oauth2.sas.grant.external.mapper.ExternalIdentityGrantRequestMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface ExternalIdentityGrantRequestMapStructMapper
        extends ExternalIdentityGrantRequestMapper {
    @Override
    @Mapping(target = "scope", source = "scope")
    @Mapping(target = "bindExternalIdentity", expression =
            "java(credentialBinding == ExternalCredentialBinding.BIND)")
    ExternalIdentityGrantRequest toGrantRequest(
            ExternalLoginCommand command,
            String scope,
            ExternalCredentialBinding credentialBinding
    );

    @Override
    ExternalAuthenticationCommand toAuthenticationCommand(ExternalIdentityGrantRequest request);
}
