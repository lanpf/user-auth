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
    public Result<PermissionApiResponse> savePermission(SavePermissionApiCommand command) {
        return Result.success(mapper.toResponse(
                catalogCommandService.savePermission(mapper.toCommand(command))));
    }

    @Override
    public Result<PermissionApiResponse> activatePermission(ChangePermissionStatusApiCommand command) {
        return Result.success(mapper.toResponse(
                catalogCommandService.activatePermission(command.permissionCode())));
    }

    @Override
    public Result<PermissionApiResponse> disablePermission(ChangePermissionStatusApiCommand command) {
        return Result.success(mapper.toResponse(
                catalogCommandService.disablePermission(command.permissionCode())));
    }

    @Override
    public Result<RoleApiResponse> saveRole(SaveRoleApiCommand command) {
        return Result.success(mapper.toResponse(
                catalogCommandService.saveRole(mapper.toCommand(command))));
    }

    @Override
    public Result<RoleApiResponse> activateRole(ChangeRoleStatusApiCommand command) {
        return Result.success(mapper.toResponse(
                catalogCommandService.activateRole(command.roleCode())));
    }

    @Override
    public Result<RoleApiResponse> disableRole(ChangeRoleStatusApiCommand command) {
        return Result.success(mapper.toResponse(
                catalogCommandService.disableRole(command.roleCode())));
    }

    @Override
    public Result<ChannelAuthorizationPolicyApiResponse> saveChannelPolicy(
            SaveChannelAuthorizationPolicyApiCommand command
    ) {
        return Result.success(mapper.toResponse(commandService.save(mapper.toCommand(command))));
    }

    @Override
    public Result<ChannelAuthorizationPolicyApiResponse> activateChannelPolicy(
            ChangeChannelAuthorizationPolicyStatusApiCommand command
    ) {
        return Result.success(mapper.toResponse(commandService.activate(new
                ChangeChannelAuthorizationPolicyStatusCommand(
                        command.channelCode(), command.expectedVersion()))));
    }

    @Override
    public Result<ChannelAuthorizationPolicyApiResponse> disableChannelPolicy(
            ChangeChannelAuthorizationPolicyStatusApiCommand command
    ) {
        return Result.success(mapper.toResponse(commandService.disable(new
                ChangeChannelAuthorizationPolicyStatusCommand(
                        command.channelCode(), command.expectedVersion()))));
    }

    @Override
    public Result<ReconcileChannelAuthorizationPolicyApiCommandOutput> reconcileChannelPolicy(
            ReconcileChannelAuthorizationPolicyApiCommand command
    ) {
        ReconcileChannelAuthorizationPolicyOutput output = commandService.reconcile(
                new ReconcileChannelAuthorizationPolicyCommand(command.channelCode(), command.batchSize()));
        return Result.success(new ReconcileChannelAuthorizationPolicyApiCommandOutput(
                output.processedUserCount(), output.policyVersion(), output.hasPendingUsers()));
    }

}
