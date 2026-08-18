package com.cloud.userauth.api.constants;

/** user-auth 签发 OAuth2 Access Token 时使用的自定义声明名称。 */
public final class AccessTokenClaimApiConstants {
    public static final String USER_ID_CLAIM = "user_id";
    public static final String AUTH_ACCOUNT_ID_CLAIM = "auth_account_id";
    public static final String SESSION_ID_CLAIM = "session_id";

    private AccessTokenClaimApiConstants() {
    }
}
