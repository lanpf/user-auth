package com.cloud.userauth.infrastructure.oauth2.sas.protocol;

import com.cloud.userauth.api.constants.JwtApiConstants;
public final class SasAuthorizationAttributes {
    private SasAuthorizationAttributes() {
    }
    public static final String USER_ID = JwtApiConstants.USER_ID_CLAIM;
    public static final String AUTH_ACCOUNT_ID = JwtApiConstants.AUTH_ACCOUNT_ID_CLAIM;
    public static final String SESSION_ID = JwtApiConstants.SESSION_ID_CLAIM;
    public static final String CLIENT_APP_ID = "client_app_id";
}
