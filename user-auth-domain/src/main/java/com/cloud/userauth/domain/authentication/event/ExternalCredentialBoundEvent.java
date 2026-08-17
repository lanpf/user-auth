package com.cloud.userauth.domain.authentication.event;

import com.cloud.userauth.domain.authentication.account.AuthAccountId;
import com.cloud.framework.domain.AbstractDomainEvent;
import com.cloud.framework.domain.DomainEventId;
import com.cloud.userauth.domain.authentication.credential.CredentialIssuer;
import com.cloud.userauth.domain.authentication.credential.Principal;
import java.time.Instant;
import lombok.Getter;

@Getter
public class ExternalCredentialBoundEvent extends AbstractDomainEvent {
    private final AuthAccountId authAccountId;
    private final CredentialIssuer issuer;
    private final Principal externalPrincipal;

    public ExternalCredentialBoundEvent(
            DomainEventId eventId,
            Instant occurredAt,
            AuthAccountId authAccountId,
            CredentialIssuer issuer,
            Principal externalPrincipal
    ) {
        super(eventId, occurredAt);
        this.authAccountId = authAccountId;
        this.issuer = issuer;
        this.externalPrincipal = externalPrincipal;
    }
}
