package com.cloud.userauth.domain.authentication.credential;

import com.cloud.userauth.domain.common.DomainError;
import com.cloud.userauth.domain.common.DomainException;

public record LoginMobile(String value) {
    public LoginMobile {
        if (value == null || value.trim().isEmpty()) {
            throw new DomainException(DomainError.AUTH_ACCOUNT_MOBILE_REQUIRED);
        }
        String normalized = value.trim();
        if (!normalized.matches("^1\\d{10}$")) {
            throw new DomainException(DomainError.AUTH_ACCOUNT_MOBILE_INVALID);
        }
        value = normalized;
    }
}
