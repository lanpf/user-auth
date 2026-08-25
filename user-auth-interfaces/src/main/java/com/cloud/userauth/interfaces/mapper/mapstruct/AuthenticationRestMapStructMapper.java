package com.cloud.userauth.interfaces.mapper.mapstruct;

import com.cloud.framework.core.AuthenticatedSessionClientRequest;
import com.cloud.framework.core.mapper.MapStructConfig;
import com.cloud.userauth.api.authentication.BindExternalCredentialApiCommand;
import com.cloud.userauth.api.authentication.LogoutApiCommand;
import com.cloud.userauth.interfaces.mapper.AuthenticationRestMapper;
import com.cloud.userauth.interfaces.rest.UserAuthenticationController;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface AuthenticationRestMapStructMapper extends AuthenticationRestMapper {
    @Override
    @Mapping(target = "authenticatedUserId", source = "userId")
    @Mapping(target = "authenticatedSessionId", source = "sessionId")
    BindExternalCredentialApiCommand toCommand(
            UserAuthenticationController.BindExternalCredentialRequest request);

    @Override
    @Mapping(target = "authenticatedUserId", source = "userId")
    LogoutApiCommand toLogoutCommand(AuthenticatedSessionClientRequest request);
}
