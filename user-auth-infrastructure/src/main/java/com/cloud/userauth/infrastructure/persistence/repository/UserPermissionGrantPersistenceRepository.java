package com.cloud.userauth.infrastructure.persistence.repository;

import com.cloud.userauth.domain.authorization.GrantId;
import com.cloud.userauth.domain.authorization.GrantSource;
import com.cloud.userauth.domain.authorization.PermissionCode;
import com.cloud.userauth.domain.authorization.UserPermissionGrant;
import com.cloud.userauth.domain.user.UserId;
import java.util.List;
import java.util.Optional;

public interface UserPermissionGrantPersistenceRepository {
    void save(UserPermissionGrant grant);
    Optional<UserPermissionGrant> findPermissionGrantById(GrantId id);
    List<UserPermissionGrant> findActivePermissionGrantsByUserId(UserId userId);
    Optional<UserPermissionGrant> findActivePermissionGrantByUserIdAndPermissionCodeAndSource(
            UserId userId, PermissionCode permissionCode, GrantSource source);
    List<UserPermissionGrant> findActivePermissionGrantsByUserIdAndSource(UserId userId, GrantSource source);
}
