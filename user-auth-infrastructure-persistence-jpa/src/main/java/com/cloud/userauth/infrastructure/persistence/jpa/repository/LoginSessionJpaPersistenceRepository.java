package com.cloud.userauth.infrastructure.persistence.jpa.repository;
import com.cloud.userauth.domain.authentication.session.*;
import com.cloud.userauth.domain.user.UserId;
import com.cloud.userauth.infrastructure.persistence.jpa.mapper.UserAuthPersistenceMapper;
import com.cloud.userauth.infrastructure.persistence.repository.LoginSessionPersistenceRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
@RequiredArgsConstructor
public class LoginSessionJpaPersistenceRepository implements LoginSessionPersistenceRepository {
    private final LoginSessionJpaRepository repository; private final UserAuthPersistenceMapper mapper;
    @Override public void save(LoginSession value) { repository.save(mapper.toDataObject(value)); }
    @Override public Optional<LoginSession> findById(SessionId id) { return repository.findById(id.value()).map(mapper::toDomain); }
    @Override public List<LoginSession> findActiveByUserId(UserId id) { return repository.findByUserIdAndStatus(id.value(), SessionStatus.ACTIVE.name()).stream().map(mapper::toDomain).toList(); }
    @Override public List<LoginSession> findActiveByUserIdAndDevice(UserId id, String deviceId) { return repository.findByUserIdAndDeviceIdAndStatus(id.value(), deviceId, SessionStatus.ACTIVE.name()).stream().map(mapper::toDomain).toList(); }
}
