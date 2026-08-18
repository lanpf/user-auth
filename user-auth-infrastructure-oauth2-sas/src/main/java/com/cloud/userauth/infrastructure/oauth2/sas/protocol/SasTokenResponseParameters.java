package com.cloud.userauth.infrastructure.oauth2.sas.protocol;

import com.cloud.userauth.api.constants.AccessTokenClaimApiConstants;
public final class SasTokenResponseParameters {
    private SasTokenResponseParameters() {
    }
    public static final String USER_ID = AccessTokenClaimApiConstants.USER_ID_CLAIM;
    public static final String AUTH_ACCOUNT_ID = AccessTokenClaimApiConstants.AUTH_ACCOUNT_ID_CLAIM;
    public static final String SESSION_ID = AccessTokenClaimApiConstants.SESSION_ID_CLAIM;
    public static final String FROM_REGISTRATION_FLOW = "from_registration_flow";
    public static final String REPLAYED = "replayed";
}
