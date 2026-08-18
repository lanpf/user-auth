package com.cloud.userauth.infrastructure.oauth2.sas.login.mapper.mapstruct;

import com.cloud.framework.core.mapper.MapStructConfig;
import com.cloud.userauth.application.login.external.ExternalLoginOutput;
import com.cloud.userauth.infrastructure.oauth2.sas.login.mapper.SasExternalTokenEndpointPayloadMapper;
import com.cloud.userauth.infrastructure.oauth2.sas.login.tokenendpoint.SasTokenEndpointPayload;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
public interface SasExternalTokenEndpointPayloadMapStructMapper
        extends SasExternalTokenEndpointPayloadMapper {
    @Override
    ExternalLoginOutput toOutput(SasTokenEndpointPayload response);
}
