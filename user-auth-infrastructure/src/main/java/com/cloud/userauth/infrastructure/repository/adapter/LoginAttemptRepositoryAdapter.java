package com.cloud.userauth.infrastructure.repository.adapter;

import com.cloud.userauth.domain.authentication.credential.CredentialIssuer;
import com.cloud.userauth.domain.authentication.credential.Principal;
import com.cloud.userauth.domain.authentication.loginattempt.LoginAttempt;
import com.cloud.userauth.domain.authentication.loginattempt.LoginAttemptId;
import com.cloud.userauth.domain.authentication.loginattempt.LoginAttemptRepository;
import com.cloud.userauth.infrastructure.persistence.repository.LoginAttemptPersistenceRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LoginAttemptRepositoryAdapter implements LoginAttemptRepository {
    private final LoginAttemptPersistenceRepository persistenceRepository;

    @Override
    public LoginAttemptId nextId() {
        return new LoginAttemptId(UUID.randomUUID().toString());
    }

    @Override
    public void save(LoginAttempt session) {
        persistenceRepository.save(session);
    }

    @Override
    public Optional<LoginAttempt> findById(LoginAttemptId id) {
        return persistenceRepository.findById(id);
    }

    @Override
    public Optional<LoginAttempt> findPendingByIssuerAndExternalPrincipal(
            CredentialIssuer issuer,
            Principal externalPrincipal
    ) {
        return persistenceRepository.findPendingByIssuerAndExternalPrincipal(issuer, externalPrincipal);
    }
}
