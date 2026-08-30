package com.cloud.userauth.interfaces.rest;

import com.cloud.userauth.api.constants.UserAuthPathApiConstants;
import com.cloud.framework.core.ClientRequest;
import com.cloud.framework.core.PaginationRequest;
import com.cloud.framework.core.PageResult;
import com.cloud.framework.core.Result;
import com.cloud.userauth.api.authorization.ChannelAuthorizationPolicyApiResponse;
import com.cloud.userauth.api.authorization.ReconcileChannelAuthorizationPolicyApiCommandOutput;
import com.cloud.userauth.api.authorization.PermissionApiResponse;
import com.cloud.userauth.api.authorization.RoleApiResponse;
import com.cloud.userauth.api.authorization.UserAuthorizationApiQueryView;
import com.cloud.userauth.api.facade.UserAuthorizationCommandFacade;
import com.cloud.userauth.api.facade.UserAuthorizationQueryFacade;
import com.cloud.userauth.interfaces.mapper.AuthorizationRestMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserAuthorizationController {
    private final UserAuthorizationCommandFacade commandFacade;
    private final UserAuthorizationQueryFacade queryFacade;
    private final AuthorizationRestMapper mapper;

    @PutMapping(UserAuthPathApiConstants.ADMIN_AUTHORIZATION_PERMISSIONS)
    public Result<PermissionApiResponse> savePermission(
            @Valid @RequestBody SavePermissionRequest request
    ) {
        return commandFacade.savePermission(mapper.toCommand(request));
    }

    @PostMapping(UserAuthPathApiConstants.ADMIN_AUTHORIZATION_PERMISSIONS_ACTIVATE)
    public Result<PermissionApiResponse> activatePermission(
            @Valid @RequestBody PermissionTargetRequest request
    ) {
        return commandFacade.activatePermission(mapper.toPermissionStatusCommand(request));
    }

    @PostMapping(UserAuthPathApiConstants.ADMIN_AUTHORIZATION_PERMISSIONS_DISABLE)
    public Result<PermissionApiResponse> disablePermission(
            @Valid @RequestBody PermissionTargetRequest request
    ) {
        return commandFacade.disablePermission(mapper.toPermissionStatusCommand(request));
    }

    @PostMapping(UserAuthPathApiConstants.ADMIN_AUTHORIZATION_PERMISSIONS_QUERY)
    public Result<PermissionApiResponse> getPermission(
            @Valid @RequestBody PermissionTargetRequest request
    ) {
        return queryFacade.getPermission(mapper.toQuery(request));
    }

    @PostMapping(UserAuthPathApiConstants.ADMIN_AUTHORIZATION_PERMISSIONS_QUERY_PAGE)
    public PageResult<PermissionApiResponse> getPermissions(
            @Valid @RequestBody CatalogPageRequest request
    ) {
        return queryFacade.getPermissions(mapper.toQuery(request));
    }

    @PutMapping(UserAuthPathApiConstants.ADMIN_AUTHORIZATION_ROLES)
    public Result<RoleApiResponse> saveRole(@Valid @RequestBody SaveRoleRequest request) {
        return commandFacade.saveRole(mapper.toCommand(request));
    }

    @PostMapping(UserAuthPathApiConstants.ADMIN_AUTHORIZATION_ROLES_ACTIVATE)
    public Result<RoleApiResponse> activateRole(@Valid @RequestBody RoleTargetRequest request) {
        return commandFacade.activateRole(mapper.toRoleStatusCommand(request));
    }

    @PostMapping(UserAuthPathApiConstants.ADMIN_AUTHORIZATION_ROLES_DISABLE)
    public Result<RoleApiResponse> disableRole(@Valid @RequestBody RoleTargetRequest request) {
        return commandFacade.disableRole(mapper.toRoleStatusCommand(request));
    }

    @PostMapping(UserAuthPathApiConstants.ADMIN_AUTHORIZATION_ROLES_QUERY)
    public Result<RoleApiResponse> getRole(@Valid @RequestBody RoleTargetRequest request) {
        return queryFacade.getRole(mapper.toQuery(request));
    }

    @PostMapping(UserAuthPathApiConstants.ADMIN_AUTHORIZATION_ROLES_QUERY_PAGE)
    public PageResult<RoleApiResponse> getRoles(@Valid @RequestBody CatalogPageRequest request) {
        return queryFacade.getRoles(mapper.toQuery(request));
    }

    @PutMapping(UserAuthPathApiConstants.ADMIN_AUTHORIZATION_CHANNEL_POLICIES)
    public Result<ChannelAuthorizationPolicyApiResponse> saveChannelPolicy(
            @Valid @RequestBody SavePolicyRequest request
    ) {
        return commandFacade.saveChannelPolicy(mapper.toCommand(request));
    }

    @PostMapping(UserAuthPathApiConstants.ADMIN_AUTHORIZATION_CHANNEL_POLICIES_ACTIVATE)
    public Result<ChannelAuthorizationPolicyApiResponse> activateChannelPolicy(
            @Valid @RequestBody VersionRequest request
    ) {
        return commandFacade.activateChannelPolicy(mapper.toStatusCommand(request));
    }

    @PostMapping(UserAuthPathApiConstants.ADMIN_AUTHORIZATION_CHANNEL_POLICIES_DISABLE)
    public Result<ChannelAuthorizationPolicyApiResponse> disableChannelPolicy(
            @Valid @RequestBody VersionRequest request
    ) {
        return commandFacade.disableChannelPolicy(mapper.toStatusCommand(request));
    }

    @PostMapping(UserAuthPathApiConstants.ADMIN_AUTHORIZATION_CHANNEL_POLICIES_RECONCILIATION)
    public Result<ReconcileChannelAuthorizationPolicyApiCommandOutput> reconcileChannelPolicy(
            @Valid @RequestBody ReconciliationRequest request
    ) {
        return commandFacade.reconcileChannelPolicy(mapper.toReconcileCommand(request));
    }

    @PostMapping(UserAuthPathApiConstants.ADMIN_AUTHORIZATION_CHANNEL_POLICIES_QUERY)
    public Result<ChannelAuthorizationPolicyApiResponse> getChannelPolicy(
            @Valid @RequestBody ChannelTargetRequest request
    ) {
        return queryFacade.getChannelPolicy(mapper.toQuery(request));
    }

    @PostMapping(UserAuthPathApiConstants.ADMIN_AUTHORIZATION_USERS_QUERY)
    public Result<UserAuthorizationApiQueryView> getUserAuthorization(
            @Valid @RequestBody UserTargetRequest request
    ) {
        return queryFacade.getUserAuthorization(mapper.toQuery(request));
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class UserTargetRequest extends ClientRequest {
        @Positive
        @NotNull
        private Long targetUserId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ChannelTargetRequest extends ClientRequest {
        @NotBlank
        private String targetChannelCode;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class PermissionTargetRequest extends ClientRequest {
        @NotBlank
        private String targetPermissionCode;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class SavePermissionRequest extends PermissionTargetRequest {
        @NotBlank
        private String permissionName;
        @NotBlank
        private String ownerService;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class RoleTargetRequest extends ClientRequest {
        @NotBlank
        private String targetRoleCode;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class SaveRoleRequest extends RoleTargetRequest {
        @NotBlank
        private String roleName;
        private List<String> permissionCodes;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class CatalogPageRequest extends ClientRequest implements PaginationRequest {
        private Integer pageNo = PaginationRequest.DEFAULT_PAGE_NO;
        private Integer pageSize = PaginationRequest.DEFAULT_PAGE_SIZE;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class SavePolicyRequest extends ChannelTargetRequest {
        private Long expectedVersion;
        private List<String> roleCodes;
        private List<String> directPermissionCodes;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class VersionRequest extends ChannelTargetRequest {
        @NotNull
        private Long expectedVersion;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ReconciliationRequest extends ChannelTargetRequest {
        @NotNull
        @Positive
        private Integer batchSize;
    }
}
