package com.cloud.userauth.domain.authentication.credential;

import com.cloud.userauth.domain.common.DomainError;
import com.cloud.userauth.domain.common.DomainException;

public record CredentialIssuer(String code, CredentialIssuerType issuerType) {
    public static final CredentialIssuer LOCAL = new CredentialIssuer(CredentialIssuerType.LOCAL.name(), CredentialIssuerType.LOCAL);

    public CredentialIssuer {
        if (code == null || code.trim().isEmpty() || issuerType == null) {
            throw new DomainException(DomainError.DOMAIN_FIELD_REQUIRED);
        }
        code = code.trim().toUpperCase();
    }

    public boolean isLocal() {
        return issuerType == CredentialIssuerType.LOCAL;
    }

    @Override
    public String toString() {
        return code;
    }
}
