package com.cloud.userauth.domain.authentication.event;

import com.cloud.userauth.domain.authentication.account.AuthAccountId;
import com.cloud.framework.domain.AbstractDomainEvent;
import com.cloud.userauth.domain.authentication.credential.CredentialKey;
import java.time.Instant;
import lombok.Getter;

@Getter
public class CredentialBoundEvent extends AbstractDomainEvent {
    private final AuthAccountId authAccountId;
    private final CredentialKey credentialKey;

    public CredentialBoundEvent(
            Instant occurredAt, AuthAccountId authAccountId, CredentialKey credentialKey
    ) {
        super(occurredAt);
        this.authAccountId = authAccountId;
        this.credentialKey = credentialKey;
    }
}
