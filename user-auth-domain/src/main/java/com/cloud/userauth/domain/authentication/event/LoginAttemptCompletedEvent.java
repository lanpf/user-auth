package com.cloud.userauth.domain.authentication.event;

import com.cloud.framework.domain.AbstractDomainEvent;
import com.cloud.framework.domain.DomainEventId;
import com.cloud.userauth.domain.authentication.loginattempt.LoginAttemptId;
import java.time.Instant;
import lombok.Getter;

@Getter
public class LoginAttemptCompletedEvent extends AbstractDomainEvent {
    private final LoginAttemptId loginAttemptId;

    public LoginAttemptCompletedEvent(DomainEventId eventId, Instant occurredAt, LoginAttemptId loginAttemptId) {
        super(eventId, occurredAt);
        this.loginAttemptId = loginAttemptId;
    }
}
