package com.cloud.userauth.interfaces.mapper.mapstruct;

import com.cloud.framework.core.mapper.MapStructConfig;
import com.cloud.userauth.interfaces.mapper.AuthorizationAdminRestMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface AuthorizationAdminRestMapStructMapper extends AuthorizationAdminRestMapper {
    @Override
    @Mapping(target = "permissionCode", source = "targetPermissionCode")
    com.cloud.userauth.api.authorization.SavePermissionApiCommand toCommand(
            com.cloud.userauth.interfaces.rest.UserAuthorizationAdminController.SavePermissionRequest request);

    @Override
    @Mapping(target = "permissionCode", source = "targetPermissionCode")
    com.cloud.userauth.api.authorization.ChangePermissionStatusApiCommand toPermissionStatusCommand(
            com.cloud.userauth.interfaces.rest.UserAuthorizationAdminController.PermissionTargetRequest request);

    @Override
    @Mapping(target = "permissionCode", source = "targetPermissionCode")
    com.cloud.userauth.api.authorization.PermissionApiQuery toQuery(
            com.cloud.userauth.interfaces.rest.UserAuthorizationAdminController.PermissionTargetRequest request);

    @Override
    @Mapping(target = "roleCode", source = "targetRoleCode")
    com.cloud.userauth.api.authorization.SaveRoleApiCommand toCommand(
            com.cloud.userauth.interfaces.rest.UserAuthorizationAdminController.SaveRoleRequest request);

    @Override
    @Mapping(target = "roleCode", source = "targetRoleCode")
    com.cloud.userauth.api.authorization.ChangeRoleStatusApiCommand toRoleStatusCommand(
            com.cloud.userauth.interfaces.rest.UserAuthorizationAdminController.RoleTargetRequest request);

    @Override
    @Mapping(target = "roleCode", source = "targetRoleCode")
    com.cloud.userauth.api.authorization.RoleApiQuery toQuery(
            com.cloud.userauth.interfaces.rest.UserAuthorizationAdminController.RoleTargetRequest request);

    @Override
    @Mapping(target = "channelCode", source = "targetChannelCode")
    com.cloud.userauth.api.authorization.SaveChannelAuthorizationPolicyApiCommand toCommand(
            com.cloud.userauth.interfaces.rest.UserAuthorizationAdminController.SavePolicyRequest request);

    @Override
    @Mapping(target = "channelCode", source = "targetChannelCode")
    com.cloud.userauth.api.authorization.ChangeChannelAuthorizationPolicyStatusApiCommand toStatusCommand(
            com.cloud.userauth.interfaces.rest.UserAuthorizationAdminController.VersionRequest request);

    @Override
    @Mapping(target = "channelCode", source = "targetChannelCode")
    com.cloud.userauth.api.authorization.ReconcileChannelAuthorizationPolicyApiCommand toReconcileCommand(
            com.cloud.userauth.interfaces.rest.UserAuthorizationAdminController.ReconciliationRequest request);

    @Override
    @Mapping(target = "channelCode", source = "targetChannelCode")
    com.cloud.userauth.api.authorization.ChannelAuthorizationPolicyApiQuery toQuery(
            com.cloud.userauth.interfaces.rest.UserAuthorizationAdminController.ChannelTargetRequest request);

    @Override
    @Mapping(target = "userId", source = "targetUserId")
    com.cloud.userauth.api.authorization.UserAuthorizationApiQuery toQuery(
            com.cloud.userauth.interfaces.rest.UserAuthorizationAdminController.UserTargetRequest request);
}
