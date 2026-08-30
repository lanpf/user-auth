package com.cloud.userauth.domain.authentication.external;

import com.cloud.framework.core.validation.Require;
import com.cloud.userauth.domain.common.DomainError;
import com.cloud.userauth.domain.common.DomainException;
import com.cloud.userauth.domain.authentication.credential.CredentialIssuer;
import com.cloud.userauth.domain.authentication.credential.LoginMobile;
import com.cloud.userauth.domain.authentication.credential.Principal;

public record ExternalIdentity(
        CredentialIssuer issuer,
        Principal principal,
        LoginMobile mobile,
        boolean mobileVerified
) {
    public ExternalIdentity {
        Require.notNull(
                issuer,
                () -> new DomainException(DomainError.AUTH_ACCOUNT_EXTERNAL_IDENTITY_INVALID));
        Require.notNull(
                principal,
                () -> new DomainException(DomainError.AUTH_ACCOUNT_EXTERNAL_IDENTITY_INVALID));
    }

    public boolean hasVerifiedMobile() {
        return mobile != null && mobileVerified;
    }

}
