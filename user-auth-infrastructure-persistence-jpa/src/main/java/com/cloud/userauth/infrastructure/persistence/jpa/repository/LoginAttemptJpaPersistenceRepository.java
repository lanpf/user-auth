package com.cloud.userauth.infrastructure.persistence.jpa.repository;

import com.cloud.userauth.domain.authentication.credential.CredentialIssuer;
import com.cloud.userauth.domain.authentication.credential.Principal;
import com.cloud.userauth.domain.authentication.loginattempt.LoginAttempt;
import com.cloud.userauth.domain.authentication.loginattempt.LoginAttemptId;
import com.cloud.userauth.infrastructure.persistence.jpa.mapper.UserAuthPersistenceMapper;
import com.cloud.userauth.infrastructure.persistence.repository.LoginAttemptPersistenceRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LoginAttemptJpaPersistenceRepository implements LoginAttemptPersistenceRepository {
    private final LoginAttemptJpaRepository repository;
    private final UserAuthPersistenceMapper mapper;

    @Override
    public void save(LoginAttempt session) {
        repository.save(mapper.toDataObject(session));
    }

    @Override
    public Optional<LoginAttempt> findById(LoginAttemptId id) {
        return repository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public Optional<LoginAttempt> findPendingByIssuerAndExternalPrincipal(
            CredentialIssuer issuer,
            Principal externalPrincipal
    ) {
        return repository
                .findFirstByIssuerAndExternalPrincipalAndStatusInOrderByCreatedAtDesc(
                        issuer.code(),
                        externalPrincipal.value(),
                        List.of("PENDING_MOBILE", "READY"))
                .map(mapper::toDomain);
    }
}
