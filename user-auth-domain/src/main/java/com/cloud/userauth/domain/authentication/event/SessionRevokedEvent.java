package com.cloud.userauth.domain.authentication.event;

import com.cloud.framework.domain.AbstractDomainEvent;
import com.cloud.userauth.domain.authentication.session.SessionId;
import com.cloud.userauth.domain.user.UserId;
import java.time.Instant;
import lombok.Getter;

@Getter
public class SessionRevokedEvent extends AbstractDomainEvent {
    private final UserId userId;
    private final SessionId sessionId;

    public SessionRevokedEvent(Instant occurredAt, UserId userId, SessionId sessionId) {
        super(occurredAt);
        this.userId = userId;
        this.sessionId = sessionId;
    }
}
