package com.cloud.userauth.infrastructure.external;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cloud.userauth.domain.authentication.credential.CredentialIssuer;
import com.cloud.userauth.domain.authentication.credential.CredentialIssuerType;
import com.cloud.userauth.infrastructure.config.ExternalIdentityProperties;
import org.junit.jupiter.api.Test;

class DefaultIssuerMobileTrustPolicyProviderTest {

    @Test
    void shouldResolveTrustedMobilePolicyByIssuerCodeIgnoringCase() {
        ExternalIdentityProperties properties = new ExternalIdentityProperties();
        ExternalIdentityProperties.IssuerPolicyProperties policy =
                new ExternalIdentityProperties.IssuerPolicyProperties();
        policy.setTrustVerifiedMobile(true);
        properties.getIssuerPolicies().put("wechat_mini_program", policy);
        DefaultIssuerMobileTrustPolicyProvider provider =
                new DefaultIssuerMobileTrustPolicyProvider(properties);

        assertTrue(provider.policyFor(issuer("WECHAT_MINI_PROGRAM")).trustVerifiedMobile());
        assertFalse(provider.policyFor(issuer("PARTNER_A")).trustVerifiedMobile());
    }

    private static CredentialIssuer issuer(String code) {
        return new CredentialIssuer(code, CredentialIssuerType.PUBLIC_THIRD_PARTY);
    }
}
