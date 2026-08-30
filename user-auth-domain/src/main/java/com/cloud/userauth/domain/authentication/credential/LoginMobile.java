package com.cloud.userauth.domain.authentication.credential;

import com.cloud.framework.core.validation.Require;
import com.cloud.userauth.domain.common.DomainError;
import com.cloud.userauth.domain.common.DomainException;

public record LoginMobile(String value) {
    public LoginMobile {
        value = Require.notBlank(value, () -> new DomainException(DomainError.AUTH_ACCOUNT_MOBILE_REQUIRED)).trim();
        if (!value.matches("^1\\d{10}$")) {
            throw new DomainException(DomainError.AUTH_ACCOUNT_MOBILE_INVALID);
        }
    }
}
