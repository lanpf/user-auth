package com.cloud.userauth.domain.authentication.service;

import com.cloud.userauth.domain.authentication.account.AuthAccount;
import com.cloud.framework.domain.DomainEvent;
import com.cloud.framework.domain.DomainEffect;
import java.util.List;

public record AuthAccountCreationEffect(AuthAccount authAccount, List<DomainEvent> events)
        implements DomainEffect {
    public AuthAccountCreationEffect {
        events = List.copyOf(events);
    }
}
