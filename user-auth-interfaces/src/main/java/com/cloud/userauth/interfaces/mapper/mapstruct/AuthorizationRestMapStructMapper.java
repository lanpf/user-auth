package com.cloud.userauth.interfaces.mapper.mapstruct;

import com.cloud.framework.core.mapper.MapStructConfig;
import com.cloud.userauth.interfaces.mapper.AuthorizationRestMapper;
import com.cloud.userauth.interfaces.rest.UserAuthorizationController;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface AuthorizationRestMapStructMapper extends AuthorizationRestMapper {
    @Override
    @Mapping(target = "permissionCode", source = "targetPermissionCode")
    com.cloud.userauth.api.authorization.SavePermissionApiCommand toCommand(
            UserAuthorizationController.SavePermissionRequest request);

    @Override
    @Mapping(target = "permissionCode", source = "targetPermissionCode")
    com.cloud.userauth.api.authorization.ChangePermissionStatusApiCommand toPermissionStatusCommand(
            UserAuthorizationController.PermissionTargetRequest request);

    @Override
    @Mapping(target = "permissionCode", source = "targetPermissionCode")
    com.cloud.userauth.api.authorization.PermissionApiQuery toQuery(
            UserAuthorizationController.PermissionTargetRequest request);

    @Override
    @Mapping(target = "roleCode", source = "targetRoleCode")
    com.cloud.userauth.api.authorization.SaveRoleApiCommand toCommand(
            UserAuthorizationController.SaveRoleRequest request);

    @Override
    @Mapping(target = "roleCode", source = "targetRoleCode")
    com.cloud.userauth.api.authorization.ChangeRoleStatusApiCommand toRoleStatusCommand(
            UserAuthorizationController.RoleTargetRequest request);

    @Override
    @Mapping(target = "roleCode", source = "targetRoleCode")
    com.cloud.userauth.api.authorization.RoleApiQuery toQuery(
            UserAuthorizationController.RoleTargetRequest request);

    @Override
    @Mapping(target = "channelCode", source = "targetChannelCode")
    com.cloud.userauth.api.authorization.SaveChannelAuthorizationPolicyApiCommand toCommand(
            UserAuthorizationController.SavePolicyRequest request);

    @Override
    @Mapping(target = "channelCode", source = "targetChannelCode")
    com.cloud.userauth.api.authorization.ChangeChannelAuthorizationPolicyStatusApiCommand toStatusCommand(
            UserAuthorizationController.VersionRequest request);

    @Override
    @Mapping(target = "channelCode", source = "targetChannelCode")
    com.cloud.userauth.api.authorization.ReconcileChannelAuthorizationPolicyApiCommand toReconcileCommand(
            UserAuthorizationController.ReconciliationRequest request);

    @Override
    @Mapping(target = "channelCode", source = "targetChannelCode")
    com.cloud.userauth.api.authorization.ChannelAuthorizationPolicyApiQuery toQuery(
            UserAuthorizationController.ChannelTargetRequest request);

    @Override
    @Mapping(target = "userId", source = "targetUserId")
    com.cloud.userauth.api.authorization.UserAuthorizationApiQuery toQuery(
            UserAuthorizationController.UserTargetRequest request);
}
