package com.cloud.userauth.domain.authentication.event;

import com.cloud.framework.domain.AbstractDomainEvent;
import com.cloud.userauth.domain.authentication.credential.CredentialIssuer;
import com.cloud.userauth.domain.authentication.credential.Principal;
import com.cloud.userauth.domain.authentication.loginattempt.LoginAttemptId;
import java.time.Instant;
import lombok.Getter;

@Getter
public class LoginAttemptCreatedEvent extends AbstractDomainEvent {
    private final LoginAttemptId loginAttemptId;
    private final CredentialIssuer issuer;
    private final Principal principal;

    public LoginAttemptCreatedEvent(
            Instant occurredAt,
            LoginAttemptId loginAttemptId,
            CredentialIssuer issuer,
            Principal principal
    ) {
        super(occurredAt);
        this.loginAttemptId = loginAttemptId;
        this.issuer = issuer;
        this.principal = principal;
    }
}
