package com.cloud.userauth.domain.authorization;

import com.cloud.framework.domain.Repository;
import com.cloud.userauth.domain.user.UserId;
import java.util.List;
import java.util.Optional;

public interface UserRoleGrantRepository extends Repository<UserRoleGrant, GrantId> {
    List<UserRoleGrant> findActiveByUserId(UserId userId);

    Optional<UserRoleGrant> findActiveByUserIdAndRoleCodeAndSource(
            UserId userId, RoleCode roleCode, GrantSource source);

    List<UserRoleGrant> findActiveByUserIdAndSource(UserId userId, GrantSource source);

    boolean existsActiveByUserIdAndRoleCodeAndSource(UserId userId, RoleCode roleCode, GrantSource source);
}
