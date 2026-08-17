package com.cloud.userauth.infrastructure.persistence.jpa.repository;
import com.cloud.userauth.application.registration.*;
import com.cloud.userauth.domain.authentication.challenge.AuthChallengeId;
import com.cloud.userauth.infrastructure.persistence.jpa.mapper.UserAuthPersistenceMapper;
import com.cloud.userauth.infrastructure.persistence.repository.RegistrationProcessPersistenceRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
@RequiredArgsConstructor
public class RegistrationProcessJpaPersistenceRepository implements RegistrationProcessPersistenceRepository {
    private final RegistrationProcessJpaRepository repository; private final UserAuthPersistenceMapper mapper;
    @Override public void save(RegistrationProcess value) { repository.save(mapper.toDataObject(value)); }
    @Override public Optional<RegistrationProcess> findById(RegistrationProcessId id) { return repository.findById(id.value()).map(mapper::toDomain); }
    @Override public Optional<RegistrationProcess> findByChallengeId(AuthChallengeId id) { return repository.findByChallengeId(id.value()).map(mapper::toDomain); }
}
