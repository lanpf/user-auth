package com.cloud.userauth.domain.user;

import com.cloud.framework.domain.EntityId;

public final class UserId extends EntityId<Long> {
    public UserId(Long value) {
        super(value);
    }
}
