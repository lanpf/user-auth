package com.cloud.userauth.interfaces.facade;

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
import com.cloud.userauth.api.facade.UserAuthorizationCommandFacade;
import com.cloud.userauth.application.authorization.ChangeChannelAuthorizationPolicyStatusCommand;
import com.cloud.userauth.application.authorization.ChannelAuthorizationPolicyCommandService;
import com.cloud.userauth.application.authorization.ReconcileChannelAuthorizationPolicyCommand;
import com.cloud.userauth.application.authorization.ReconcileChannelAuthorizationPolicyOutput;
import com.cloud.userauth.application.authorization.AuthorizationCatalogCommandService;
import com.cloud.userauth.interfaces.mapper.AuthorizationApiMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@RequiredArgsConstructor
public class DefaultUserAuthorizationCommandFacade implements UserAuthorizationCommandFacade {
    private final ChannelAuthorizationPolicyCommandService commandService;
    private final AuthorizationCatalogCommandService catalogCommandService;
    private final AuthorizationApiMapper mapper;

    @Override
    public Result<PermissionApiResponse> savePermission(SavePermissionApiCommand request) {
        return Result.success(mapper.toResponse(
                catalogCommandService.savePermission(mapper.toCommand(request))));
    }

    @Override
    public Result<PermissionApiResponse> activatePermission(ChangePermissionStatusApiCommand request) {
        return Result.success(mapper.toResponse(
                catalogCommandService.activatePermission(request.permissionCode())));
    }

    @Override
    public Result<PermissionApiResponse> disablePermission(ChangePermissionStatusApiCommand request) {
        return Result.success(mapper.toResponse(
                catalogCommandService.disablePermission(request.permissionCode())));
    }

    @Override
    public Result<RoleApiResponse> saveRole(SaveRoleApiCommand request) {
        return Result.success(mapper.toResponse(
                catalogCommandService.saveRole(mapper.toCommand(request))));
    }

    @Override
    public Result<RoleApiResponse> activateRole(ChangeRoleStatusApiCommand request) {
        return Result.success(mapper.toResponse(
                catalogCommandService.activateRole(request.roleCode())));
    }

    @Override
    public Result<RoleApiResponse> disableRole(ChangeRoleStatusApiCommand request) {
        return Result.success(mapper.toResponse(
                catalogCommandService.disableRole(request.roleCode())));
    }

    @Override
    public Result<ChannelAuthorizationPolicyApiResponse> saveChannelPolicy(
            SaveChannelAuthorizationPolicyApiCommand request
    ) {
        return Result.success(mapper.toResponse(commandService.save(mapper.toCommand(request))));
    }

    @Override
    public Result<ChannelAuthorizationPolicyApiResponse> activateChannelPolicy(
            ChangeChannelAuthorizationPolicyStatusApiCommand request
    ) {
        return Result.success(mapper.toResponse(commandService.activate(new
                ChangeChannelAuthorizationPolicyStatusCommand(
                        request.channelCode(), request.expectedVersion()))));
    }

    @Override
    public Result<ChannelAuthorizationPolicyApiResponse> disableChannelPolicy(
            ChangeChannelAuthorizationPolicyStatusApiCommand request
    ) {
        return Result.success(mapper.toResponse(commandService.disable(new
                ChangeChannelAuthorizationPolicyStatusCommand(
                        request.channelCode(), request.expectedVersion()))));
    }

    @Override
    public Result<ReconcileChannelAuthorizationPolicyApiCommandOutput> reconcileChannelPolicy(
            ReconcileChannelAuthorizationPolicyApiCommand request
    ) {
        ReconcileChannelAuthorizationPolicyOutput output = commandService.reconcile(
                new ReconcileChannelAuthorizationPolicyCommand(request.channelCode(), request.batchSize()));
        return Result.success(new ReconcileChannelAuthorizationPolicyApiCommandOutput(
                output.processedUserCount(), output.policyVersion(), output.hasPendingUsers()));
    }

}
