package com.cloud.userauth.infrastructure.oauth2.sas.grant.external;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;

public final class ExternalIdentityGrantAuthenticationToken
        extends OAuth2AuthorizationGrantAuthenticationToken {
    private final ExternalIdentityGrantRequest request;

    public ExternalIdentityGrantAuthenticationToken(
            Authentication clientPrincipal,
            ExternalIdentityGrantRequest request
    ) {
        super(
                ExternalIdentityGrantTypes.EXTERNAL_IDENTITY,
                clientPrincipal,
                ExternalIdentityGrantParameterConverter.toAdditionalParameters(request));
        this.request = request;
    }

    public ExternalIdentityGrantRequest request() {
        return request;
    }
}
