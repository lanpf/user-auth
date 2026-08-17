package com.cloud.userauth.domain.authentication.event;

import com.cloud.framework.domain.AbstractDomainEvent;
import com.cloud.framework.domain.DomainEventId;
import com.cloud.userauth.domain.authentication.credential.CredentialKey;
import java.time.Instant;
import lombok.Getter;

@Getter
public class CredentialVerifiedEvent extends AbstractDomainEvent {
    private final CredentialKey credentialKey;

    public CredentialVerifiedEvent(DomainEventId eventId, Instant occurredAt, CredentialKey credentialKey) {
        super(eventId, occurredAt);
        this.credentialKey = credentialKey;
    }
}
