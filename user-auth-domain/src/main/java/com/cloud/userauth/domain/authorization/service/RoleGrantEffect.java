package com.cloud.userauth.domain.authorization.service;

import com.cloud.userauth.domain.authorization.UserRoleGrant;
import com.cloud.framework.domain.DomainEvent;
import com.cloud.framework.domain.DomainEffect;
import java.util.List;

public record RoleGrantEffect(UserRoleGrant grant, List<DomainEvent> events) implements DomainEffect {
    public RoleGrantEffect {
        events = List.copyOf(events);
    }
}
