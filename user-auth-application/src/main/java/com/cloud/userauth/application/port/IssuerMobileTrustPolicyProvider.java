package com.cloud.userauth.application.port;

import com.cloud.userauth.domain.authentication.credential.CredentialIssuer;
import com.cloud.userauth.domain.authentication.external.IssuerMobileTrustPolicy;

public interface IssuerMobileTrustPolicyProvider {
    IssuerMobileTrustPolicy policyFor(CredentialIssuer issuer);
}
