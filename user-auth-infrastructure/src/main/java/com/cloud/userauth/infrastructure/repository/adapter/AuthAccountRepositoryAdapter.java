package com.cloud.userauth.infrastructure.repository.adapter;

import com.cloud.framework.id.LongIdGenerator;
import com.cloud.userauth.domain.authentication.account.AuthAccount;
import com.cloud.userauth.domain.authentication.account.AuthAccountId;
import com.cloud.userauth.domain.authentication.account.AuthAccountRepository;
import com.cloud.userauth.domain.authentication.credential.CredentialKey;
import com.cloud.userauth.domain.user.UserId;
import com.cloud.userauth.infrastructure.id.IdGeneratorNames;
import com.cloud.userauth.infrastructure.persistence.repository.AuthAccountPersistenceRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AuthAccountRepositoryAdapter implements AuthAccountRepository {
    private final LongIdGenerator idGenerator;
    private final AuthAccountPersistenceRepository persistenceRepository;

    @Override public AuthAccountId nextId() { return new AuthAccountId(idGenerator.nextId(IdGeneratorNames.AUTH_ACCOUNT)); }
    @Override public void save(AuthAccount aggregate) { persistenceRepository.save(aggregate); }
    @Override public Optional<AuthAccount> findById(AuthAccountId id) { return persistenceRepository.findById(id); }
    @Override public Optional<AuthAccount> findByUserId(UserId id) { return persistenceRepository.findByUserId(id); }
    @Override public Optional<AuthAccount> findByCredential(CredentialKey key) { return persistenceRepository.findByCredential(key); }
    @Override public boolean existsActiveByUserId(UserId id) { return persistenceRepository.existsActiveByUserId(id); }
    @Override public boolean existsActiveCredential(CredentialKey key) { return persistenceRepository.existsActiveCredential(key); }
}
