package com.cloud.userauth.infrastructure.repository.adapter;

import com.cloud.userauth.domain.authentication.session.*;
import com.cloud.userauth.domain.user.UserId;
import com.cloud.userauth.infrastructure.persistence.repository.LoginSessionPersistenceRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LoginSessionRepositoryAdapter implements LoginSessionRepository {
    private final LoginSessionPersistenceRepository persistenceRepository;
    @Override public SessionId nextId() { return new SessionId(UUID.randomUUID().toString()); }
    @Override public void save(LoginSession session) { persistenceRepository.save(session); }
    @Override public Optional<LoginSession> findById(SessionId id) { return persistenceRepository.findById(id); }
    @Override public List<LoginSession> findActiveByUserId(UserId id) { return persistenceRepository.findActiveByUserId(id); }
    @Override public List<LoginSession> findActiveByUserIdAndDevice(UserId id, String deviceId) { return persistenceRepository.findActiveByUserIdAndDevice(id, deviceId); }
}
