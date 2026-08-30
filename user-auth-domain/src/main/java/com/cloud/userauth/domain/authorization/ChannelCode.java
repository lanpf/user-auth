package com.cloud.userauth.domain.authorization;

import com.cloud.userauth.domain.common.DomainException;
import com.cloud.userauth.domain.common.StringEntityId;

public final class ChannelCode extends StringEntityId {
    public ChannelCode(String value) {
        super(value);
    }

    @Override
    protected String validate(String value) {
        String trimmed = super.validate(value);
        if (!trimmed.matches("[A-Za-z0-9][A-Za-z0-9._-]*")) {
            throw DomainException.invalidEntityId();
        }
        return trimmed;
    }
}
