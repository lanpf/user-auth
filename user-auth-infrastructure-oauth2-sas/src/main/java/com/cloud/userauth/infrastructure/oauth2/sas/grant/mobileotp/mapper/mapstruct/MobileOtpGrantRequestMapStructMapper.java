package com.cloud.userauth.infrastructure.oauth2.sas.grant.mobileotp.mapper.mapstruct;

import com.cloud.framework.core.mapper.MapStructConfig;
import com.cloud.userauth.application.login.MobileOtpAuthenticationCommand;
import com.cloud.userauth.application.login.MobileOtpLoginCommand;
import com.cloud.userauth.infrastructure.oauth2.sas.grant.mobileotp.MobileOtpGrantRequest;
import com.cloud.userauth.infrastructure.oauth2.sas.grant.mobileotp.mapper.MobileOtpGrantRequestMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface MobileOtpGrantRequestMapStructMapper
        extends MobileOtpGrantRequestMapper {

    @Override
    @Mapping(target = "scope", source = "scope")
    MobileOtpGrantRequest toGrantRequest(
            MobileOtpLoginCommand command,
            String scope
    );

    @Override
    MobileOtpAuthenticationCommand toAuthenticationCommand(
            MobileOtpGrantRequest request
    );
}
