package com.cloud.userauth.domain.authorization.event;

import com.cloud.userauth.domain.authorization.RoleCode;
import com.cloud.framework.domain.AbstractDomainEvent;
import java.time.Instant;
import lombok.Getter;

@Getter
public class RoleCreatedEvent extends AbstractDomainEvent {
    private final RoleCode roleCode;

    public RoleCreatedEvent(Instant occurredAt, RoleCode roleCode) {
        super(occurredAt);
        this.roleCode = roleCode;
    }
}
