package com.cloud.userauth.domain.authentication.event;

import com.cloud.framework.domain.AbstractDomainEvent;
import com.cloud.framework.domain.DomainEventId;
import com.cloud.userauth.domain.authentication.session.SessionId;
import com.cloud.userauth.domain.user.UserId;
import java.time.Instant;
import lombok.Getter;

@Getter
public class UserLoggedOutEvent extends AbstractDomainEvent {
    private final UserId userId;
    private final SessionId sessionId;

    public UserLoggedOutEvent(DomainEventId eventId, Instant occurredAt, UserId userId, SessionId sessionId) {
        super(eventId, occurredAt);
        this.userId = userId;
        this.sessionId = sessionId;
    }
}
