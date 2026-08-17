package com.cloud.userauth.domain.authentication.service;

import com.cloud.framework.domain.DomainEvent;
import com.cloud.framework.domain.DomainEffect;
import com.cloud.userauth.domain.authentication.session.LoginSession;
import java.util.List;

public record SessionRevocationEffect(
        LoginSession session,
        List<DomainEvent> events
) implements DomainEffect {
    public SessionRevocationEffect {
        events = List.copyOf(events);
    }
}
