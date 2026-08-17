package com.cloud.userauth.infrastructure.repository.adapter;

import com.cloud.userauth.domain.authorization.Role;
import com.cloud.userauth.domain.authorization.RoleCode;
import com.cloud.userauth.domain.authorization.RoleRepository;
import com.cloud.userauth.infrastructure.persistence.repository.RolePersistenceRepository;
import java.util.Optional;
import com.cloud.framework.domain.PagedList;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RoleRepositoryAdapter implements RoleRepository {
    private final RolePersistenceRepository persistenceRepository;

    @Override public RoleCode nextId() { throw new UnsupportedOperationException("RoleCode is assigned"); }
    @Override public void save(Role role) { persistenceRepository.save(role); }
    @Override public Optional<Role> findById(RoleCode id) { return persistenceRepository.findById(id); }
    @Override public Optional<Role> findByRoleCode(RoleCode roleCode) { return findById(roleCode); }
    @Override public boolean existsByRoleCode(RoleCode roleCode) { return findById(roleCode).isPresent(); }
    @Override public PagedList<Role> findRoles(int pageNo, int pageSize) {
        return persistenceRepository.findRoles(pageNo, pageSize);
    }
}
