package com.cloud.userauth.interfaces.facade;

import com.cloud.framework.core.PageQuery;
import com.cloud.framework.core.PageResult;
import com.cloud.framework.core.Result;
import com.cloud.framework.domain.PagedList;
import com.cloud.userauth.api.authorization.ChannelAuthorizationPolicyApiQuery;
import com.cloud.userauth.api.authorization.AuthorizationCatalogApiQuery;
import com.cloud.userauth.api.authorization.ChannelAuthorizationPolicyApiResponse;
import com.cloud.userauth.api.authorization.UserAuthorizationApiQuery;
import com.cloud.userauth.api.authorization.UserAuthorizationApiQueryView;
import com.cloud.userauth.api.authorization.PermissionApiQuery;
import com.cloud.userauth.api.authorization.PermissionApiResponse;
import com.cloud.userauth.api.authorization.RoleApiQuery;
import com.cloud.userauth.api.authorization.RoleApiResponse;
import com.cloud.userauth.api.facade.UserAuthorizationQueryFacade;
import com.cloud.userauth.application.authorization.AuthorizationQueryService;
import com.cloud.userauth.application.authorization.PermissionView;
import com.cloud.userauth.application.authorization.RoleView;
import com.cloud.userauth.interfaces.mapper.AuthorizationApiMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DefaultUserAuthorizationQueryFacade implements UserAuthorizationQueryFacade {
    private final AuthorizationQueryService queryService;
    private final AuthorizationApiMapper mapper;

    @Override
    public Result<PermissionApiResponse> getPermission(PermissionApiQuery query) {
        return Result.success(mapper.toResponse(
                queryService.findPermission(query.permissionCode())));
    }

    @Override
    public PageResult<PermissionApiResponse> getPermissions(AuthorizationCatalogApiQuery query) {
        PagedList<PermissionView> page =
                queryService.findPermissions(PageQuery.from(query));
        return PageResult.success(page.getData().stream().map(mapper::toResponse).toList(),
                page.getTotal());
    }

    @Override
    public Result<RoleApiResponse> getRole(RoleApiQuery query) {
        return Result.success(mapper.toResponse(queryService.findRole(query.roleCode())));
    }

    @Override
    public PageResult<RoleApiResponse> getRoles(AuthorizationCatalogApiQuery query) {
        PagedList<RoleView> page =
                queryService.findRoles(PageQuery.from(query));
        return PageResult.success(page.getData().stream().map(mapper::toResponse).toList(),
                page.getTotal());
    }

    @Override
    public Result<ChannelAuthorizationPolicyApiResponse> getChannelPolicy(
            ChannelAuthorizationPolicyApiQuery query
    ) {
        return Result.success(mapper.toResponse(queryService.findPolicy(query.channelCode())));
    }

    @Override
    public Result<UserAuthorizationApiQueryView> getUserAuthorization(
            UserAuthorizationApiQuery query
    ) {
        return Result.success(mapper.toView(
                queryService.findUserAuthorization(query.userId())));
    }
}
