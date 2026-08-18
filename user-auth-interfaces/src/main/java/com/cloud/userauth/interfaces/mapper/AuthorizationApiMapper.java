package com.cloud.userauth.interfaces.mapper;

import com.cloud.userauth.api.authorization.ChannelAuthorizationPolicyApiResponse;
import com.cloud.userauth.api.authorization.SaveChannelAuthorizationPolicyApiCommand;
import com.cloud.userauth.api.authorization.UserAuthorizationApiQueryView;
import com.cloud.userauth.api.authorization.PermissionApiResponse;
import com.cloud.userauth.api.authorization.RoleApiResponse;
import com.cloud.userauth.api.authorization.SavePermissionApiCommand;
import com.cloud.userauth.api.authorization.SaveRoleApiCommand;
import com.cloud.userauth.application.authorization.ChannelAuthorizationPolicyView;
import com.cloud.userauth.application.authorization.PermissionView;
import com.cloud.userauth.application.authorization.RoleView;
import com.cloud.userauth.application.authorization.SavePermissionCommand;
import com.cloud.userauth.application.authorization.SaveRoleCommand;
import com.cloud.userauth.application.authorization.SaveChannelAuthorizationPolicyCommand;
import com.cloud.userauth.application.authorization.UserAuthorizationView;

public interface AuthorizationApiMapper {
    SaveChannelAuthorizationPolicyCommand toCommand(
            SaveChannelAuthorizationPolicyApiCommand command);
    ChannelAuthorizationPolicyApiResponse toResponse(
            ChannelAuthorizationPolicyView view);
    UserAuthorizationApiQueryView toView(UserAuthorizationView view);
    SavePermissionCommand toCommand(SavePermissionApiCommand command);
    SaveRoleCommand toCommand(SaveRoleApiCommand command);
    PermissionApiResponse toResponse(PermissionView view);
    RoleApiResponse toResponse(RoleView view);
}
