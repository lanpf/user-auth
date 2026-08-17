package com.cloud.userauth.infrastructure.persistence.repository;

import com.cloud.userauth.domain.authorization.Role;
import com.cloud.userauth.domain.authorization.RoleCode;
import com.cloud.framework.domain.PagedList;
import java.util.Optional;

public interface RolePersistenceRepository {
    void save(Role role);
    Optional<Role> findById(RoleCode roleCode);
    PagedList<Role> findRoles(int pageNo, int pageSize);
}
