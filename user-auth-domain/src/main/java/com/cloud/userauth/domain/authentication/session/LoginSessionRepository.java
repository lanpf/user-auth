package com.cloud.userauth.domain.authentication.session;

import com.cloud.framework.domain.Repository;
import com.cloud.userauth.domain.user.UserId;
import java.util.List;

public interface LoginSessionRepository extends Repository<LoginSession, SessionId> {
    List<LoginSession> findActiveByUserId(UserId userId);

    List<LoginSession> findActiveByUserIdAndDevice(UserId userId, String deviceId);
}
