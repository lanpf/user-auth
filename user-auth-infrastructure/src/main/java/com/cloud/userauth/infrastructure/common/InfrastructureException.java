package com.cloud.userauth.infrastructure.common;

import com.cloud.framework.core.error.BaseException;

public class InfrastructureException extends BaseException {

    public InfrastructureException(InfrastructureError error) {
        super(error);
    }

    public InfrastructureException(InfrastructureError error, Throwable cause) {
        super(error, cause);
    }
}
