package com.cloud.userauth.application.registration;

import com.cloud.userauth.domain.authentication.challenge.AuthChallengeId;
import java.util.Optional;

public interface RegistrationProcessRepository {
    RegistrationProcessId nextId();

    void save(RegistrationProcess process);

    Optional<RegistrationProcess> findById(RegistrationProcessId id);

    Optional<RegistrationProcess> findByChallengeId(AuthChallengeId challengeId);
}
