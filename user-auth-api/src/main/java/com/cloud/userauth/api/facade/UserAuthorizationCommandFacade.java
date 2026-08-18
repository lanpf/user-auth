package com.cloud.userauth.api.facade;

import com.cloud.framework.core.Result;
import com.cloud.userauth.api.authorization.ChangeChannelAuthorizationPolicyStatusApiCommand;
import com.cloud.userauth.api.authorization.ChangePermissionStatusApiCommand;
import com.cloud.userauth.api.authorization.ChangeRoleStatusApiCommand;
import com.cloud.userauth.api.authorization.ChannelAuthorizationPolicyApiResponse;
import com.cloud.userauth.api.authorization.ReconcileChannelAuthorizationPolicyApiCommand;
import com.cloud.userauth.api.authorization.ReconcileChannelAuthorizationPolicyApiCommandOutput;
import com.cloud.userauth.api.authorization.PermissionApiResponse;
import com.cloud.userauth.api.authorization.RoleApiResponse;
import com.cloud.userauth.api.authorization.SaveChannelAuthorizationPolicyApiCommand;
import com.cloud.userauth.api.authorization.SavePermissionApiCommand;
import com.cloud.userauth.api.authorization.SaveRoleApiCommand;
import jakarta.validation.Valid;

public interface UserAuthorizationCommandFacade {
    Result<PermissionApiResponse> savePermission(@Valid SavePermissionApiCommand command);
    Result<PermissionApiResponse> activatePermission(@Valid ChangePermissionStatusApiCommand command);
    Result<PermissionApiResponse> disablePermission(@Valid ChangePermissionStatusApiCommand command);
    Result<RoleApiResponse> saveRole(@Valid SaveRoleApiCommand command);
    Result<RoleApiResponse> activateRole(@Valid ChangeRoleStatusApiCommand command);
    Result<RoleApiResponse> disableRole(@Valid ChangeRoleStatusApiCommand command);
    Result<ChannelAuthorizationPolicyApiResponse> saveChannelPolicy(
            @Valid SaveChannelAuthorizationPolicyApiCommand command);
    Result<ChannelAuthorizationPolicyApiResponse> activateChannelPolicy(
            @Valid ChangeChannelAuthorizationPolicyStatusApiCommand command);
    Result<ChannelAuthorizationPolicyApiResponse> disableChannelPolicy(
            @Valid ChangeChannelAuthorizationPolicyStatusApiCommand command);

    Result<ReconcileChannelAuthorizationPolicyApiCommandOutput> reconcileChannelPolicy(
            @Valid ReconcileChannelAuthorizationPolicyApiCommand command);
}
