package com.cloud.userauth.infrastructure.oauth2.sas.grant.mobileotp;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;

public final class MobileOtpGrantAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken {
    private final MobileOtpGrantRequest request;

    public MobileOtpGrantAuthenticationToken(
            Authentication clientPrincipal,
            MobileOtpGrantRequest request
    ) {
        super(
                MobileOtpGrantTypes.MOBILE_OTP,
                clientPrincipal,
                MobileOtpGrantParameterConverter.toAdditionalParameters(request));
        this.request = request;
    }

    public MobileOtpGrantRequest request() {
        return request;
    }
}
