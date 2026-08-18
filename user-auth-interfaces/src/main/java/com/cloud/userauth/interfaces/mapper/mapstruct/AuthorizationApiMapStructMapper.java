package com.cloud.userauth.interfaces.mapper.mapstruct;

import com.cloud.framework.core.mapper.MapStructConfig;
import com.cloud.userauth.api.authorization.ChannelAuthorizationPolicyApiResponse;
import com.cloud.userauth.api.authorization.SaveChannelAuthorizationPolicyApiCommand;
import com.cloud.userauth.api.authorization.UserAuthorizationApiQueryView;
import com.cloud.userauth.api.authorization.PermissionApiResponse;
import com.cloud.userauth.api.authorization.RoleApiResponse;
import com.cloud.userauth.api.authorization.SavePermissionApiCommand;
import com.cloud.userauth.api.authorization.SaveRoleApiCommand;
import com.cloud.userauth.application.authorization.ChannelAuthorizationPolicyView;
import com.cloud.userauth.application.authorization.SaveChannelAuthorizationPolicyCommand;
import com.cloud.userauth.application.authorization.UserAuthorizationView;
import com.cloud.userauth.application.authorization.PermissionView;
import com.cloud.userauth.application.authorization.RoleView;
import com.cloud.userauth.application.authorization.SavePermissionCommand;
import com.cloud.userauth.application.authorization.SaveRoleCommand;
import com.cloud.userauth.interfaces.mapper.AuthorizationApiMapper;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
public interface AuthorizationApiMapStructMapper extends AuthorizationApiMapper {
    @Override
    SaveChannelAuthorizationPolicyCommand toCommand(
            SaveChannelAuthorizationPolicyApiCommand request);
    @Override
    ChannelAuthorizationPolicyApiResponse toResponse(ChannelAuthorizationPolicyView view);
    @Override
    UserAuthorizationApiQueryView toView(UserAuthorizationView view);
    @Override
    SavePermissionCommand toCommand(SavePermissionApiCommand request);
    @Override
    SaveRoleCommand toCommand(SaveRoleApiCommand request);
    @Override
    PermissionApiResponse toResponse(PermissionView view);
    @Override
    RoleApiResponse toResponse(RoleView view);
}
