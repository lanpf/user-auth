package com.cloud.userauth.domain.authentication.credential;

import com.cloud.framework.core.validation.Require;
import com.cloud.userauth.domain.common.DomainException;

public record Principal(String value) {
    public Principal {
        value = Require.notBlank(value, DomainException::missingField).trim();
    }

    public static Principal mobile(LoginMobile mobile) {
        return new Principal(mobile.value());
    }

    @Override
    public String toString() {
        return value;
    }
}
