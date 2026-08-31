package com.cloud.userauth.domain.authentication.service;

import com.cloud.framework.domain.DomainEvent;
import com.cloud.userauth.domain.authentication.event.UserLoggedOutEvent;
import com.cloud.userauth.domain.authentication.session.LoginSession;
import com.cloud.userauth.domain.authentication.session.SessionStatus;
import java.time.Instant;
import java.util.List;

public class SessionDomainService {
    public SessionRevocationEffect logout(LoginSession session, Instant loggedOutAt) {
        if (session.getStatus() == SessionStatus.ACTIVE && loggedOutAt.isAfter(session.getExpiresAt())) {
            session.expire(loggedOutAt);
        }
        if (session.getStatus() != SessionStatus.ACTIVE) {
            return new SessionRevocationEffect(session, List.of());
        }
        session.revoke(loggedOutAt);
        DomainEvent event = new UserLoggedOutEvent(
                loggedOutAt, session.getUserId(), session.id());
        return new SessionRevocationEffect(session, List.of(event));
    }
}
