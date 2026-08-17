package com.cloud.userauth.infrastructure.persistence.repository;

import com.cloud.userauth.domain.authorization.GrantId;
import com.cloud.userauth.domain.authorization.GrantSource;
import com.cloud.userauth.domain.authorization.RoleCode;
import com.cloud.userauth.domain.authorization.UserRoleGrant;
import com.cloud.userauth.domain.user.UserId;
import java.util.List;
import java.util.Optional;

public interface UserRoleGrantPersistenceRepository {
    void save(UserRoleGrant grant);
    Optional<UserRoleGrant> findRoleGrantById(GrantId id);
    List<UserRoleGrant> findActiveRoleGrantsByUserId(UserId userId);
    Optional<UserRoleGrant> findActiveRoleGrantByUserIdAndRoleCodeAndSource(
            UserId userId, RoleCode roleCode, GrantSource source);
    List<UserRoleGrant> findActiveRoleGrantsByUserIdAndSource(UserId userId, GrantSource source);
}
