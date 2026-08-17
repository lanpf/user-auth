package com.cloud.userauth.domain.authentication.service;

import com.cloud.userauth.domain.authentication.account.AuthAccount;
import com.cloud.framework.domain.DomainEvent;
import com.cloud.framework.domain.DomainEffect;
import com.cloud.userauth.domain.authentication.credential.Credential;
import java.util.List;

public record CredentialChangeEffect(
        AuthAccount authAccount,
        Credential credential,
        List<DomainEvent> events
) implements DomainEffect {
    public CredentialChangeEffect {
        events = List.copyOf(events);
    }
}
