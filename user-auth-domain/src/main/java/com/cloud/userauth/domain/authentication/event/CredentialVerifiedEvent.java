package com.cloud.userauth.domain.authentication.event;

import com.cloud.framework.domain.AbstractDomainEvent;
import com.cloud.userauth.domain.authentication.credential.CredentialKey;
import java.time.Instant;
import lombok.Getter;

@Getter
public class CredentialVerifiedEvent extends AbstractDomainEvent {
    private final CredentialKey credentialKey;

    public CredentialVerifiedEvent(Instant occurredAt, CredentialKey credentialKey) {
        super(occurredAt);
        this.credentialKey = credentialKey;
    }
}
