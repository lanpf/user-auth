package com.cloud.userauth.domain.authentication.account;

import com.cloud.framework.domain.Repository;
import com.cloud.userauth.domain.authentication.credential.CredentialKey;
import com.cloud.userauth.domain.user.UserId;
import java.util.Optional;

public interface AuthAccountRepository extends Repository<AuthAccount, AuthAccountId> {
    Optional<AuthAccount> findByUserId(UserId userId);

    Optional<AuthAccount> findByCredential(CredentialKey credentialKey);

    boolean existsActiveByUserId(UserId userId);

    boolean existsActiveCredential(CredentialKey credentialKey);
}
