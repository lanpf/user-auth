package com.cloud.userauth.domain.authentication.external;

import com.cloud.userauth.domain.authentication.credential.CredentialIssuer;

public record IssuerMobileTrustPolicy(CredentialIssuer issuer, boolean trustVerifiedMobile) {
    public boolean canTrustMobile(ExternalIdentity identity) {
        return identity != null
                && identity.issuer().equals(issuer)
                && trustVerifiedMobile
                && identity.hasVerifiedMobile();
    }
}
