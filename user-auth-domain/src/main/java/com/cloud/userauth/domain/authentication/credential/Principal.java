package com.cloud.userauth.domain.authentication.credential;

import com.cloud.userauth.domain.common.DomainError;
import com.cloud.userauth.domain.common.DomainException;

public record Principal(String value) {
    public Principal {
        if (value == null || value.trim().isEmpty()) {
            throw new DomainException(DomainError.DOMAIN_FIELD_REQUIRED);
        }
        value = value.trim();
    }

    public static Principal mobile(LoginMobile mobile) {
        return new Principal(mobile.value());
    }

    @Override
    public String toString() {
        return value;
    }
}
