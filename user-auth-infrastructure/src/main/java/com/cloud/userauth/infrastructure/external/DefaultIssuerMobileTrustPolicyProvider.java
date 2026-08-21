package com.cloud.userauth.infrastructure.external;

import com.cloud.userauth.application.port.IssuerMobileTrustPolicyProvider;
import com.cloud.userauth.domain.authentication.credential.CredentialIssuer;
import com.cloud.userauth.domain.authentication.external.IssuerMobileTrustPolicy;
import com.cloud.userauth.infrastructure.config.ExternalIdentityProperties;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class DefaultIssuerMobileTrustPolicyProvider implements IssuerMobileTrustPolicyProvider {
    private final ExternalIdentityProperties properties;

    @Override
    public IssuerMobileTrustPolicy policyFor(CredentialIssuer issuer) {
        boolean trusted = properties.getIssuerPolicies().entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(issuer.code()))
                .map(Map.Entry::getValue)
                .map(ExternalIdentityProperties.IssuerPolicyProperties::getTrustVerifiedMobile)
                .anyMatch(Boolean.TRUE::equals);
        return new IssuerMobileTrustPolicy(issuer, trusted);
    }
}
