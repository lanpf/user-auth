package com.cloud.userauth.infrastructure.repository.adapter;

import com.cloud.framework.id.LongIdGenerator;
import com.cloud.userauth.domain.authorization.GrantId;
import com.cloud.userauth.domain.authorization.GrantSource;
import com.cloud.userauth.domain.authorization.RoleCode;
import com.cloud.userauth.domain.authorization.UserRoleGrant;
import com.cloud.userauth.domain.authorization.UserRoleGrantRepository;
import com.cloud.userauth.domain.user.UserId;
import com.cloud.userauth.infrastructure.id.IdGeneratorScene;
import com.cloud.userauth.infrastructure.persistence.repository.UserRoleGrantPersistenceRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserRoleGrantRepositoryAdapter implements UserRoleGrantRepository {
    private final LongIdGenerator idGenerator;
    private final UserRoleGrantPersistenceRepository persistenceRepository;

    @Override public GrantId nextId() { return new GrantId(idGenerator.nextId(IdGeneratorScene.USER_ROLE_GRANT.getValue())); }
    @Override public void save(UserRoleGrant value) { persistenceRepository.save(value); }
    @Override public Optional<UserRoleGrant> findById(GrantId id) { return persistenceRepository.findRoleGrantById(id); }
    @Override public List<UserRoleGrant> findActiveByUserId(UserId id) { return persistenceRepository.findActiveRoleGrantsByUserId(id); }
    @Override public Optional<UserRoleGrant> findActiveByUserIdAndRoleCodeAndSource(UserId u, RoleCode r, GrantSource s) { return persistenceRepository.findActiveRoleGrantByUserIdAndRoleCodeAndSource(u, r, s); }
    @Override public List<UserRoleGrant> findActiveByUserIdAndSource(UserId u, GrantSource s) { return persistenceRepository.findActiveRoleGrantsByUserIdAndSource(u, s); }
    @Override public boolean existsActiveByUserIdAndRoleCodeAndSource(UserId u, RoleCode r, GrantSource s) { return findActiveByUserIdAndRoleCodeAndSource(u, r, s).isPresent(); }
}
