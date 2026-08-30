package com.cloud.userauth.infrastructure.oauth2.sas.protocol;

import com.cloud.userauth.api.constants.AccessTokenClaimApiConstants;
public final class SasAuthorizationAttributes {
    private SasAuthorizationAttributes() {
    }
    public static final String USER_ID = "user_id";
    public static final String AUTH_ACCOUNT_ID = AccessTokenClaimApiConstants.AUTH_ACCOUNT_ID_CLAIM;
    public static final String SESSION_ID = AccessTokenClaimApiConstants.SESSION_ID_CLAIM;
    public static final String CLIENT_APP_ID = SasGrantParameterNames.CLIENT_APP_ID;
}
