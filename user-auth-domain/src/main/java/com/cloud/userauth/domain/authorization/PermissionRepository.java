package com.cloud.userauth.domain.authorization;

import com.cloud.framework.domain.Repository;
import com.cloud.framework.domain.PagedList;

import java.util.Collection;
import java.util.List;

public interface PermissionRepository extends Repository<Permission, PermissionCode> {
    @Override
    PermissionCode nextId();

    List<Permission> findByIds(Collection<PermissionCode> ids);

    PagedList<Permission> findPermissions(int pageNo, int pageSize);
}
