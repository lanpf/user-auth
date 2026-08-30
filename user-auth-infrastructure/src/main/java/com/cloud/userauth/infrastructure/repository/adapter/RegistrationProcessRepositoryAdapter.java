package com.cloud.userauth.infrastructure.repository.adapter;

import com.cloud.framework.id.LongIdGenerator;
import com.cloud.userauth.application.registration.*;
import com.cloud.userauth.domain.authentication.challenge.AuthChallengeId;
import com.cloud.userauth.infrastructure.id.IdGeneratorScene;
import com.cloud.userauth.infrastructure.persistence.repository.RegistrationProcessPersistenceRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RegistrationProcessRepositoryAdapter implements RegistrationProcessRepository {
    private final LongIdGenerator idGenerator;
    private final RegistrationProcessPersistenceRepository persistenceRepository;
    @Override public RegistrationProcessId nextId() { return new RegistrationProcessId(idGenerator.nextId(IdGeneratorScene.REGISTRATION_PROCESS.getValue())); }
    @Override public void save(RegistrationProcess process) { persistenceRepository.save(process); }
    @Override public Optional<RegistrationProcess> findById(RegistrationProcessId id) { return persistenceRepository.findById(id); }
    @Override public Optional<RegistrationProcess> findByChallengeId(AuthChallengeId id) { return persistenceRepository.findByChallengeId(id); }
}
