package com.cloud.userauth.infrastructure.oauth2.sas.login.mapper.mapstruct;

import com.cloud.framework.core.mapper.MapStructConfig;
import com.cloud.userauth.application.login.MobileOtpLoginOutput;
import com.cloud.userauth.infrastructure.oauth2.sas.login.tokenendpoint.SasTokenEndpointPayload;
import com.cloud.userauth.infrastructure.oauth2.sas.login.mapper.SasTokenEndpointPayloadMapper;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
public interface SasTokenEndpointPayloadMapStructMapper
        extends SasTokenEndpointPayloadMapper {

    @Override
    MobileOtpLoginOutput toOutput(SasTokenEndpointPayload response);
}
