package com.cloud.userauth.domain.authorization;

import com.cloud.userauth.domain.common.DomainError;
import com.cloud.userauth.domain.common.DomainException;
import com.cloud.userauth.domain.common.StringEntityId;

public final class PermissionCode extends StringEntityId {
    public PermissionCode(String value) {
        super(value);
    }

    @Override
    protected String validate(String value) {
        String trimmed = super.validate(value);
        if (!trimmed.matches("[a-z0-9][a-z0-9.-]*:[a-z0-9][a-z0-9.-]*")) {
            throw new DomainException(DomainError.PERMISSION_CODE_INVALID);
        }
        return trimmed;
    }
}
