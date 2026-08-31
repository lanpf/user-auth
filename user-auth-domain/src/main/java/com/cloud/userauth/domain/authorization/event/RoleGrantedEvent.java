package com.cloud.userauth.domain.authorization.event;

import com.cloud.userauth.domain.authorization.GrantId;
import com.cloud.userauth.domain.authorization.RoleCode;
import com.cloud.framework.domain.AbstractDomainEvent;
import com.cloud.userauth.domain.user.UserId;
import java.time.Instant;
import lombok.Getter;

@Getter
public class RoleGrantedEvent extends AbstractDomainEvent {
    private final GrantId grantId;
    private final UserId userId;
    private final RoleCode roleCode;

    public RoleGrantedEvent(Instant occurredAt, GrantId grantId, UserId userId, RoleCode roleCode) {
        super(occurredAt);
        this.grantId = grantId;
        this.userId = userId;
        this.roleCode = roleCode;
    }
}
