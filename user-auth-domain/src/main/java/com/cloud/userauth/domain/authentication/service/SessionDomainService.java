package com.cloud.userauth.domain.authentication.service;

import com.cloud.framework.domain.DomainEvent;
import com.cloud.framework.domain.DomainEventIdGenerator;
import com.cloud.userauth.domain.authentication.event.UserLoggedOutEvent;
import com.cloud.userauth.domain.authentication.session.LoginSession;
import com.cloud.userauth.domain.authentication.session.SessionStatus;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SessionDomainService {
    private final DomainEventIdGenerator domainEventIdGenerator;

    public SessionRevocationEffect logout(LoginSession session, Instant loggedOutAt) {
        if (session.getStatus() == SessionStatus.ACTIVE && loggedOutAt.isAfter(session.getExpiresAt())) {
            session.expire(loggedOutAt);
        }
        if (session.getStatus() != SessionStatus.ACTIVE) {
            return new SessionRevocationEffect(session, List.of());
        }
        session.revoke(loggedOutAt);
        DomainEvent event = new UserLoggedOutEvent(
                domainEventIdGenerator.nextId(), loggedOutAt, session.getUserId(), session.id());
        return new SessionRevocationEffect(session, List.of(event));
    }
}
