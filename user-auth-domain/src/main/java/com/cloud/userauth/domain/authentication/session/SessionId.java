package com.cloud.userauth.domain.authentication.session;

import com.cloud.framework.domain.EntityId;

public final class SessionId extends EntityId<String> {
    public SessionId(String value) {
        super(value);
    }
}
