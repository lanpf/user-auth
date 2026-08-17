package com.cloud.userauth.infrastructure.persistence.repository;

import com.cloud.userauth.domain.authentication.account.AuthAccount;
import com.cloud.userauth.domain.authentication.account.AuthAccountId;
import com.cloud.userauth.domain.authentication.credential.CredentialKey;
import com.cloud.userauth.domain.user.UserId;
import java.util.Optional;

public interface AuthAccountPersistenceRepository {
    void save(AuthAccount account);
    Optional<AuthAccount> findById(AuthAccountId id);
    Optional<AuthAccount> findByUserId(UserId userId);
    Optional<AuthAccount> findByCredential(CredentialKey key);
    boolean existsActiveByUserId(UserId userId);
    boolean existsActiveCredential(CredentialKey key);
}
