package com.cloud.userauth.api.constants;

/** user-auth 签发 JWT 时使用的自定义声明名称。 */
public final class JwtApiConstants {
    private JwtApiConstants() {
    }
    public static final String USER_ID_CLAIM = "user_id";
    public static final String AUTH_ACCOUNT_ID_CLAIM = "auth_account_id";
    public static final String SESSION_ID_CLAIM = "session_id";
}
