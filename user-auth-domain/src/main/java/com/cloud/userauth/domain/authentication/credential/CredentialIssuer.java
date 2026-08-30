package com.cloud.userauth.domain.authentication.credential;

import com.cloud.framework.core.validation.Require;
import com.cloud.userauth.domain.common.DomainException;

public record CredentialIssuer(String code, CredentialIssuerType issuerType) {
    public static final CredentialIssuer LOCAL = new CredentialIssuer(CredentialIssuerType.LOCAL.name(), CredentialIssuerType.LOCAL);

    public CredentialIssuer {
        code = Require.notBlank(code, DomainException::missingField).trim().toUpperCase();
        Require.notNull(issuerType, DomainException::missingField);
    }

    public boolean isLocal() {
        return issuerType == CredentialIssuerType.LOCAL;
    }

    @Override
    public String toString() {
        return code;
    }
}
