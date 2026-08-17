package com.cloud.userauth.domain.authorization;

import com.cloud.framework.domain.EntityId;

public final class GrantId extends EntityId<Long> {
    public GrantId(Long value) {
        super(value);
    }
}
