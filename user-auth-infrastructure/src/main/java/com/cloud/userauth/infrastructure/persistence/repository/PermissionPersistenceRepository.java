package com.cloud.userauth.infrastructure.persistence.repository;

import com.cloud.userauth.domain.authorization.Permission;
import com.cloud.userauth.domain.authorization.PermissionCode;
import com.cloud.framework.domain.PagedList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PermissionPersistenceRepository {
    void save(Permission permission);
    Optional<Permission> findById(PermissionCode permissionCode);
    List<Permission> findByIds(Collection<PermissionCode> permissionCodes);
    PagedList<Permission> findPermissions(int pageNo, int pageSize);
}
