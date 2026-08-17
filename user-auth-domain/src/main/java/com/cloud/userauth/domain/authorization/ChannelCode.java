package com.cloud.userauth.domain.authorization;

import com.cloud.framework.domain.EntityId;
import com.cloud.userauth.domain.common.DomainError;
import com.cloud.userauth.domain.common.DomainException;

public final class ChannelCode extends EntityId<String> {
    public ChannelCode(String value) {
        super(validate(value));
    }

    private static String validate(String value) {
        if (value == null || !value.trim().matches("[A-Za-z0-9][A-Za-z0-9._-]*")) {
            throw new DomainException(DomainError.DOMAIN_FIELD_REQUIRED);
        }
        return value.trim();
    }
}
