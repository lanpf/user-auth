package com.cloud.userauth.infrastructure.oauth2.sas.grant.external;

import org.springframework.security.oauth2.core.AuthorizationGrantType;

public final class ExternalIdentityGrantTypes {
    public static final String VALUE = "urn:ietf:params:oauth:grant-type:external_identity";
    public static final AuthorizationGrantType EXTERNAL_IDENTITY = new AuthorizationGrantType(VALUE);

    private ExternalIdentityGrantTypes() {
    }
}
