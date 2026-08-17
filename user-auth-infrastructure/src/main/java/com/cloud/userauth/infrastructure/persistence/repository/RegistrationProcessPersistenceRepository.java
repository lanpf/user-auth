package com.cloud.userauth.infrastructure.persistence.repository;

import com.cloud.userauth.application.registration.RegistrationProcess;
import com.cloud.userauth.application.registration.RegistrationProcessId;
import com.cloud.userauth.domain.authentication.challenge.AuthChallengeId;
import java.util.Optional;

public interface RegistrationProcessPersistenceRepository {
    void save(RegistrationProcess process);
    Optional<RegistrationProcess> findById(RegistrationProcessId id);
    Optional<RegistrationProcess> findByChallengeId(AuthChallengeId challengeId);
}
