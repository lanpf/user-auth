package com.cloud.userauth.domain.common;

import com.cloud.framework.core.error.BaseException;

public class DomainException extends BaseException {

    public DomainException(DomainError error) {
        super(error);
    }

    public DomainException(DomainError error, Throwable cause) {
        super(error, cause);
    }

    public static DomainException invalidEntityId() {
        return new DomainException(DomainError.DOMAIN_ENTITY_ID_INVALID);
    }

    public static DomainException missingField() {
        return new DomainException(DomainError.DOMAIN_OBJECT_FIELD_REQUIRED);
    }
}
