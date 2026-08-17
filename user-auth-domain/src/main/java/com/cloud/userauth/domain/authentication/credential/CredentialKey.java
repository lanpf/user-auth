package com.cloud.userauth.domain.authentication.credential;

public record CredentialKey(CredentialType credentialType, CredentialIssuer issuer, Principal principal) {

    public static CredentialKey mobile(LoginMobile mobile) {
        return new CredentialKey(CredentialType.MOBILE, CredentialIssuer.LOCAL, Principal.mobile(mobile));
    }

    public static CredentialKey external(CredentialIssuer issuer, Principal principal) {
        return new CredentialKey(CredentialType.EXTERNAL, issuer, principal);
    }

}
