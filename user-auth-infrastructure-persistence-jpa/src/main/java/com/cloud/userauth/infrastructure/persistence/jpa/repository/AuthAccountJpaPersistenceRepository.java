package com.cloud.userauth.infrastructure.persistence.jpa.repository;

import com.cloud.userauth.domain.authentication.account.*;
import com.cloud.userauth.domain.authentication.credential.*;
import com.cloud.userauth.domain.user.UserId;
import com.cloud.userauth.infrastructure.persistence.jpa.mapper.UserAuthPersistenceMapper;
import com.cloud.userauth.infrastructure.persistence.jpa.model.AuthAccountDO;
import com.cloud.userauth.infrastructure.persistence.repository.AuthAccountPersistenceRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AuthAccountJpaPersistenceRepository implements AuthAccountPersistenceRepository {
    private final AuthAccountJpaRepository accounts;
    private final CredentialJpaRepository credentials;
    private final UserAuthPersistenceMapper mapper;
    @Override public void save(AuthAccount account) {
        accounts.save(mapper.toDataObject(account));
        credentials.saveAll(account.credentials().stream().map(c -> mapper.toDataObject(account.id(), c)).toList());
    }
    @Override public Optional<AuthAccount> findById(AuthAccountId id) { return accounts.findById(id.value()).map(this::aggregate); }
    @Override public Optional<AuthAccount> findByUserId(UserId id) { return accounts.findByUserId(id.value()).map(this::aggregate); }
    @Override public Optional<AuthAccount> findByCredential(CredentialKey key) {
        return credentials.findFirstByCredentialTypeAndIssuerAndPrincipalAndStatus(
                key.credentialType().name(), key.issuer().code(), key.principal().value(), CredentialStatus.ACTIVE.name())
                .flatMap(c -> accounts.findById(c.getAuthAccountId())).map(this::aggregate);
    }
    @Override public boolean existsActiveByUserId(UserId id) { return accounts.existsByUserIdAndStatus(id.value(), AuthAccountStatus.ACTIVE.name()); }
    @Override public boolean existsActiveCredential(CredentialKey key) { return credentials.existsByCredentialTypeAndIssuerAndPrincipalAndStatus(key.credentialType().name(), key.issuer().code(), key.principal().value(), CredentialStatus.ACTIVE.name()); }
    private AuthAccount aggregate(AuthAccountDO account) { return mapper.toDomain(account, credentials.findByAuthAccountIdOrderByCreatedAt(account.getId())); }
}
