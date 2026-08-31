package com.cloud.userauth.domain.authentication.event;

import com.cloud.framework.domain.AbstractDomainEvent;
import com.cloud.userauth.domain.authentication.loginattempt.LoginAttemptId;
import java.time.Instant;
import lombok.Getter;

@Getter
public class LoginAttemptCompletedEvent extends AbstractDomainEvent {
    private final LoginAttemptId loginAttemptId;

    public LoginAttemptCompletedEvent(Instant occurredAt, LoginAttemptId loginAttemptId) {
        super(occurredAt);
        this.loginAttemptId = loginAttemptId;
    }
}
