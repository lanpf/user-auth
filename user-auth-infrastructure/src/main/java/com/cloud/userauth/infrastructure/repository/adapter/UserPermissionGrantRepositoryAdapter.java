package com.cloud.userauth.infrastructure.repository.adapter;

import com.cloud.framework.id.LongIdGenerator;
import com.cloud.userauth.domain.authorization.GrantId;
import com.cloud.userauth.domain.authorization.GrantSource;
import com.cloud.userauth.domain.authorization.PermissionCode;
import com.cloud.userauth.domain.authorization.UserPermissionGrant;
import com.cloud.userauth.domain.authorization.UserPermissionGrantRepository;
import com.cloud.userauth.domain.user.UserId;
import com.cloud.userauth.infrastructure.id.IdGeneratorNames;
import com.cloud.userauth.infrastructure.persistence.repository.UserPermissionGrantPersistenceRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserPermissionGrantRepositoryAdapter implements UserPermissionGrantRepository {
    private final LongIdGenerator idGenerator;
    private final UserPermissionGrantPersistenceRepository persistenceRepository;

    @Override public GrantId nextId() { return new GrantId(idGenerator.nextId(IdGeneratorNames.USER_PERMISSION_GRANT)); }
    @Override public void save(UserPermissionGrant value) { persistenceRepository.save(value); }
    @Override public Optional<UserPermissionGrant> findById(GrantId id) { return persistenceRepository.findPermissionGrantById(id); }
    @Override public List<UserPermissionGrant> findActiveByUserId(UserId id) { return persistenceRepository.findActivePermissionGrantsByUserId(id); }
    @Override public Optional<UserPermissionGrant> findActiveByUserIdAndPermissionCodeAndSource(UserId u, PermissionCode p, GrantSource s) { return persistenceRepository.findActivePermissionGrantByUserIdAndPermissionCodeAndSource(u, p, s); }
    @Override public List<UserPermissionGrant> findActiveByUserIdAndSource(UserId u, GrantSource s) { return persistenceRepository.findActivePermissionGrantsByUserIdAndSource(u, s); }
    @Override public boolean existsActiveByUserIdAndPermissionCodeAndSource(UserId u, PermissionCode p, GrantSource s) { return findActiveByUserIdAndPermissionCodeAndSource(u, p, s).isPresent(); }
}
