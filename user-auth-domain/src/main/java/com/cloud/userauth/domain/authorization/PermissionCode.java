package com.cloud.userauth.domain.authorization;

import com.cloud.framework.domain.EntityId;
import com.cloud.userauth.domain.common.DomainError;
import com.cloud.userauth.domain.common.DomainException;

public final class PermissionCode extends EntityId<String> {
    public PermissionCode(String value) {
        super(validate(value));
    }

    private static String validate(String value) {
        if (value == null || !value.trim().matches("[a-z0-9][a-z0-9.-]*:[a-z0-9][a-z0-9.-]*")) {
            throw new DomainException(DomainError.PERMISSION_CODE_INVALID);
        }
        return value.trim();
    }
}
