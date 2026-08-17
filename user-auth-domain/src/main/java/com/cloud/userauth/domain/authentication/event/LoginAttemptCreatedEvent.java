package com.cloud.userauth.domain.authentication.event;

import com.cloud.framework.domain.AbstractDomainEvent;
import com.cloud.framework.domain.DomainEventId;
import com.cloud.userauth.domain.authentication.credential.CredentialIssuer;
import com.cloud.userauth.domain.authentication.credential.Principal;
import com.cloud.userauth.domain.authentication.loginattempt.LoginAttemptId;
import java.time.Instant;
import lombok.Getter;

@Getter
public class LoginAttemptCreatedEvent extends AbstractDomainEvent {
    private final LoginAttemptId loginAttemptId;
    private final CredentialIssuer issuer;
    private final Principal externalPrincipal;

    public LoginAttemptCreatedEvent(
            DomainEventId eventId,
            Instant occurredAt,
            LoginAttemptId loginAttemptId,
            CredentialIssuer issuer,
            Principal externalPrincipal
    ) {
        super(eventId, occurredAt);
        this.loginAttemptId = loginAttemptId;
        this.issuer = issuer;
        this.externalPrincipal = externalPrincipal;
    }
}
