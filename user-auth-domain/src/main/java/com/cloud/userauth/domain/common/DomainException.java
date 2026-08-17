package com.cloud.userauth.domain.common;

import com.cloud.framework.core.error.BaseException;

public class DomainException extends BaseException {

    public DomainException(DomainError error) {
        super(error);
    }

    public DomainException(DomainError error, Throwable cause) {
        super(error, cause);
    }
}
