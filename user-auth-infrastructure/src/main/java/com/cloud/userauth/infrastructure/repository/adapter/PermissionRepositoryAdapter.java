package com.cloud.userauth.infrastructure.repository.adapter;

import com.cloud.userauth.domain.authorization.Permission;
import com.cloud.userauth.domain.authorization.PermissionCode;
import com.cloud.userauth.domain.authorization.PermissionRepository;
import com.cloud.userauth.infrastructure.persistence.repository.PermissionPersistenceRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import com.cloud.framework.domain.PagedList;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PermissionRepositoryAdapter implements PermissionRepository {
    private final PermissionPersistenceRepository persistenceRepository;

    @Override public PermissionCode nextId() { throw new UnsupportedOperationException("PermissionCode is assigned"); }
    @Override public void save(Permission value) { persistenceRepository.save(value); }
    @Override public Optional<Permission> findById(PermissionCode id) { return persistenceRepository.findById(id); }
    @Override public List<Permission> findByIds(Collection<PermissionCode> ids) { return persistenceRepository.findByIds(ids); }
    @Override public PagedList<Permission> findPermissions(int pageNo, int pageSize) {
        return persistenceRepository.findPermissions(pageNo, pageSize);
    }
}
