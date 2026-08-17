package com.cloud.userauth.domain.authorization;

import com.cloud.framework.domain.Repository;
import com.cloud.userauth.domain.user.UserId;
import java.util.List;
import java.util.Optional;

public interface UserPermissionGrantRepository extends Repository<UserPermissionGrant, GrantId> {
    GrantId nextId();
    List<UserPermissionGrant> findActiveByUserId(UserId userId);
    Optional<UserPermissionGrant> findActiveByUserIdAndPermissionCodeAndSource(
            UserId userId, PermissionCode permissionCode, GrantSource source);
    List<UserPermissionGrant> findActiveByUserIdAndSource(UserId userId, GrantSource source);
    boolean existsActiveByUserIdAndPermissionCodeAndSource(
            UserId userId, PermissionCode permissionCode, GrantSource source);
}
