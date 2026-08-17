package com.cloud.userauth.interfaces.mapper;

import com.cloud.userauth.api.authorization.AuthorizationCatalogApiQuery;
import com.cloud.userauth.api.authorization.ChangeChannelAuthorizationPolicyStatusApiCommand;
import com.cloud.userauth.api.authorization.ChangePermissionStatusApiCommand;
import com.cloud.userauth.api.authorization.ChangeRoleStatusApiCommand;
import com.cloud.userauth.api.authorization.ChannelAuthorizationPolicyApiQuery;
import com.cloud.userauth.api.authorization.PermissionApiQuery;
import com.cloud.userauth.api.authorization.ReconcileChannelAuthorizationPolicyApiCommand;
import com.cloud.userauth.api.authorization.RoleApiQuery;
import com.cloud.userauth.api.authorization.SaveChannelAuthorizationPolicyApiCommand;
import com.cloud.userauth.api.authorization.SavePermissionApiCommand;
import com.cloud.userauth.api.authorization.SaveRoleApiCommand;
import com.cloud.userauth.api.authorization.UserAuthorizationApiQuery;
import com.cloud.userauth.interfaces.rest.UserAuthorizationAdminController;

public interface AuthorizationAdminRestMapper {
    SavePermissionApiCommand toCommand(UserAuthorizationAdminController.SavePermissionRequest request);

    ChangePermissionStatusApiCommand toPermissionStatusCommand(
            UserAuthorizationAdminController.PermissionTargetRequest request);

    PermissionApiQuery toQuery(UserAuthorizationAdminController.PermissionTargetRequest request);

    AuthorizationCatalogApiQuery toQuery(UserAuthorizationAdminController.CatalogPageRequest request);

    SaveRoleApiCommand toCommand(UserAuthorizationAdminController.SaveRoleRequest request);

    ChangeRoleStatusApiCommand toRoleStatusCommand(
            UserAuthorizationAdminController.RoleTargetRequest request);

    RoleApiQuery toQuery(UserAuthorizationAdminController.RoleTargetRequest request);

    SaveChannelAuthorizationPolicyApiCommand toCommand(
            UserAuthorizationAdminController.SavePolicyRequest request);

    ChangeChannelAuthorizationPolicyStatusApiCommand toStatusCommand(
            UserAuthorizationAdminController.VersionRequest request);

    ReconcileChannelAuthorizationPolicyApiCommand toReconcileCommand(
            UserAuthorizationAdminController.ReconciliationRequest request);

    ChannelAuthorizationPolicyApiQuery toQuery(
            UserAuthorizationAdminController.ChannelTargetRequest request);

    UserAuthorizationApiQuery toQuery(UserAuthorizationAdminController.UserTargetRequest request);
}
