package com.cloud.userauth.domain.authorization;

import com.cloud.framework.domain.Repository;
import java.util.Optional;
import com.cloud.framework.domain.PagedList;

public interface RoleRepository extends Repository<Role, RoleCode> {
    Optional<Role> findByRoleCode(RoleCode roleCode);

    boolean existsByRoleCode(RoleCode roleCode);

    PagedList<Role> findRoles(int pageNo, int pageSize);
}
