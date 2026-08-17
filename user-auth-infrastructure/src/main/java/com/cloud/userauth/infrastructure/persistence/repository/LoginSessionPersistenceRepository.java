package com.cloud.userauth.infrastructure.persistence.repository;

import com.cloud.userauth.domain.authentication.session.LoginSession;
import com.cloud.userauth.domain.authentication.session.SessionId;
import com.cloud.userauth.domain.user.UserId;
import java.util.List;
import java.util.Optional;

public interface LoginSessionPersistenceRepository {
    void save(LoginSession session);
    Optional<LoginSession> findById(SessionId id);
    List<LoginSession> findActiveByUserId(UserId userId);
    List<LoginSession> findActiveByUserIdAndDevice(UserId userId, String deviceId);
}
