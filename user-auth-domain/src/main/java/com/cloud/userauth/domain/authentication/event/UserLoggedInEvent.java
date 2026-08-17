package com.cloud.userauth.domain.authentication.event;

import com.cloud.userauth.domain.authentication.account.AuthAccountId;
import com.cloud.framework.domain.AbstractDomainEvent;
import com.cloud.framework.domain.DomainEventId;
import com.cloud.userauth.domain.authentication.session.SessionId;
import com.cloud.userauth.domain.user.UserId;
import java.time.Instant;
import lombok.Getter;

@Getter
public class UserLoggedInEvent extends AbstractDomainEvent {
    private final UserId userId;
    private final AuthAccountId authAccountId;
    private final SessionId sessionId;

    public UserLoggedInEvent(
            DomainEventId eventId, Instant occurredAt, UserId userId, AuthAccountId authAccountId, SessionId sessionId
    ) {
        super(eventId, occurredAt);
        this.userId = userId;
        this.authAccountId = authAccountId;
        this.sessionId = sessionId;
    }
}
