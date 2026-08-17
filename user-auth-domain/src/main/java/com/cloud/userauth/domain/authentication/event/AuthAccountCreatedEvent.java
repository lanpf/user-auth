package com.cloud.userauth.domain.authentication.event;

import com.cloud.userauth.domain.authentication.account.AuthAccountId;
import com.cloud.framework.domain.AbstractDomainEvent;
import com.cloud.framework.domain.DomainEventId;
import com.cloud.userauth.domain.user.UserId;
import java.time.Instant;
import lombok.Getter;

@Getter
public class AuthAccountCreatedEvent extends AbstractDomainEvent {
    private final AuthAccountId authAccountId;
    private final UserId userId;

    public AuthAccountCreatedEvent(DomainEventId eventId, Instant occurredAt, AuthAccountId authAccountId, UserId userId) {
        super(eventId, occurredAt);
        this.authAccountId = authAccountId;
        this.userId = userId;
    }
}
