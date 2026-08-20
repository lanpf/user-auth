package com.cloud.userauth.infrastructure.persistence.repository;

import com.cloud.userauth.domain.authentication.credential.CredentialIssuer;
import com.cloud.userauth.domain.authentication.credential.Principal;
import com.cloud.userauth.domain.authentication.loginattempt.LoginAttempt;
import com.cloud.userauth.domain.authentication.loginattempt.LoginAttemptId;
import java.util.Optional;

public interface LoginAttemptPersistenceRepository {
    void save(LoginAttempt session);

    Optional<LoginAttempt> findById(LoginAttemptId id);

    Optional<LoginAttempt> findPendingByIssuerAndPrincipal(
            CredentialIssuer issuer,
            Principal principal
    );
}
