package com.cloud.userauth.api.facade;

import com.cloud.framework.core.PageResult;
import com.cloud.framework.core.Result;
import com.cloud.userauth.api.authorization.ChannelAuthorizationPolicyApiQuery;
import com.cloud.userauth.api.authorization.AuthorizationCatalogApiQuery;
import com.cloud.userauth.api.authorization.ChannelAuthorizationPolicyApiResponse;
import com.cloud.userauth.api.authorization.UserAuthorizationApiQuery;
import com.cloud.userauth.api.authorization.UserAuthorizationApiQueryView;
import com.cloud.userauth.api.authorization.PermissionApiQuery;
import com.cloud.userauth.api.authorization.PermissionApiResponse;
import com.cloud.userauth.api.authorization.RoleApiQuery;
import com.cloud.userauth.api.authorization.RoleApiResponse;
import jakarta.validation.Valid;

public interface UserAuthorizationQueryFacade {
    Result<PermissionApiResponse> getPermission(@Valid PermissionApiQuery query);
    PageResult<PermissionApiResponse> getPermissions(
            @Valid AuthorizationCatalogApiQuery query);
    Result<RoleApiResponse> getRole(@Valid RoleApiQuery query);
    PageResult<RoleApiResponse> getRoles(@Valid AuthorizationCatalogApiQuery query);
    Result<ChannelAuthorizationPolicyApiResponse> getChannelPolicy(
            @Valid ChannelAuthorizationPolicyApiQuery query);

    Result<UserAuthorizationApiQueryView> getUserAuthorization(
            @Valid UserAuthorizationApiQuery query);
}
