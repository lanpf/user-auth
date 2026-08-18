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
import com.cloud.userauth.interfaces.rest.UserAuthorizationController;

public interface AuthorizationRestMapper {
    SavePermissionApiCommand toCommand(UserAuthorizationController.SavePermissionRequest request);

    ChangePermissionStatusApiCommand toPermissionStatusCommand(
            UserAuthorizationController.PermissionTargetRequest request);

    PermissionApiQuery toQuery(UserAuthorizationController.PermissionTargetRequest request);

    AuthorizationCatalogApiQuery toQuery(UserAuthorizationController.CatalogPageRequest request);

    SaveRoleApiCommand toCommand(UserAuthorizationController.SaveRoleRequest request);

    ChangeRoleStatusApiCommand toRoleStatusCommand(
            UserAuthorizationController.RoleTargetRequest request);

    RoleApiQuery toQuery(UserAuthorizationController.RoleTargetRequest request);

    SaveChannelAuthorizationPolicyApiCommand toCommand(
            UserAuthorizationController.SavePolicyRequest request);

    ChangeChannelAuthorizationPolicyStatusApiCommand toStatusCommand(
            UserAuthorizationController.VersionRequest request);

    ReconcileChannelAuthorizationPolicyApiCommand toReconcileCommand(
            UserAuthorizationController.ReconciliationRequest request);

    ChannelAuthorizationPolicyApiQuery toQuery(
            UserAuthorizationController.ChannelTargetRequest request);

    UserAuthorizationApiQuery toQuery(UserAuthorizationController.UserTargetRequest request);
}
