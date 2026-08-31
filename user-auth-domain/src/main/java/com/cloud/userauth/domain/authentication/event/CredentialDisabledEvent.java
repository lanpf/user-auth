package com.cloud.userauth.domain.authentication.event;

import com.cloud.framework.domain.AbstractDomainEvent;
import com.cloud.userauth.domain.authentication.credential.CredentialKey;
import java.time.Instant;
import lombok.Getter;

@Getter
public class CredentialDisabledEvent extends AbstractDomainEvent {
    private final CredentialKey credentialKey;

    public CredentialDisabledEvent(Instant occurredAt, CredentialKey credentialKey) {
        super(occurredAt);
        this.credentialKey = credentialKey;
    }
}
