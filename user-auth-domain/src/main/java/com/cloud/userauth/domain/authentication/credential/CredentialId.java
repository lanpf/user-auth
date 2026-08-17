package com.cloud.userauth.domain.authentication.credential;

import com.cloud.framework.domain.EntityId;

public final class CredentialId extends EntityId<Long> {
    public CredentialId(Long value) {
        super(value);
    }
}
