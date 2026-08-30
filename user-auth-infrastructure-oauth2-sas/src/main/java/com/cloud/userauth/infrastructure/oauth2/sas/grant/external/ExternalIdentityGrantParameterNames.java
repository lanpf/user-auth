package com.cloud.userauth.infrastructure.oauth2.sas.grant.external;

import com.cloud.userauth.infrastructure.oauth2.sas.protocol.SasGrantParameterNames;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;

public final class ExternalIdentityGrantParameterNames {
    public static final String LOGIN_ATTEMPT_ID = "login_attempt_id";
    public static final String CHALLENGE_ID = SasGrantParameterNames.CHALLENGE_ID;
    public static final String CODE = OAuth2ParameterNames.CODE;
    public static final String DEVICE_ID = SasGrantParameterNames.DEVICE_ID;
    public static final String DEVICE_TYPE = SasGrantParameterNames.DEVICE_TYPE;
    public static final String DEVICE_NAME = SasGrantParameterNames.DEVICE_NAME;
    public static final String CLIENT_APP_ID = SasGrantParameterNames.CLIENT_APP_ID;
    public static final String CLIENT_PLATFORM = SasGrantParameterNames.CLIENT_PLATFORM;
    public static final String CLIENT_VERSION = SasGrantParameterNames.CLIENT_VERSION;
    public static final String CHANNEL_CODE = SasGrantParameterNames.CHANNEL_CODE;
    public static final String BIND_EXTERNAL_IDENTITY = "bind_external_identity";

    private ExternalIdentityGrantParameterNames() {
    }
}
