package com.cloud.userauth.infrastructure.oauth2.sas.grant.mobileotp;

import org.springframework.security.oauth2.core.AuthorizationGrantType;

public final class MobileOtpGrantTypes {
    public static final String VALUE = "urn:ietf:params:oauth:grant-type:mobile_otp";
    public static final AuthorizationGrantType MOBILE_OTP = new AuthorizationGrantType(VALUE);

    private MobileOtpGrantTypes() {
    }
}
