package com.cloud.userauth.interfaces.mapper.mapstruct;

import com.cloud.framework.core.mapper.MapStructConfig;
import com.cloud.userauth.api.authentication.BindExternalCredentialApiCommand;
import com.cloud.userauth.interfaces.mapper.AuthenticationRestMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface AuthenticationRestMapStructMapper extends AuthenticationRestMapper {
    @Override
    @Mapping(target = "authenticatedUserId", source = "authenticatedUserId")
    @Mapping(target = "authenticatedAuthAccountId", source = "authenticatedAuthAccountId")
    @Mapping(target = "issuer", source = "request.issuer")
    @Mapping(target = "authorizationCode", source = "request.authorizationCode")
    BindExternalCredentialApiCommand toBindExternalCredentialCommand(
            com.cloud.userauth.interfaces.rest.UserAuthenticationController.BindExternalCredentialRequest request,
            Long authenticatedUserId,
            Long authenticatedAuthAccountId);
}
