package com.cloud.userauth.infrastructure.external.wechat.miniprogram.client;

final class WechatMiniProgramApiConstants {
    static final String STABLE_TOKEN_PATH = "/cgi-bin/stable_token";
    static final String CODE_TO_SESSION_PATH = "/sns/jscode2session";
    static final String PHONE_NUMBER_PATH = "/wxa/business/getuserphonenumber";

    static final String AUTHORIZATION_CODE_GRANT = "authorization_code";
    static final String CLIENT_CREDENTIAL_GRANT = "client_credential";
    static final String APP_ID_PARAMETER = "appid";
    static final String SECRET_PARAMETER = "secret";
    static final String JS_CODE_PARAMETER = "js_code";
    static final String GRANT_TYPE_PARAMETER = "grant_type";
    static final String ACCESS_TOKEN_PARAMETER = "access_token";

    static final String OPEN_ID_PROPERTY = "openid";
    static final String SESSION_KEY_PROPERTY = "session_key";
    static final String UNION_ID_PROPERTY = "unionid";
    static final String FORCE_REFRESH_PROPERTY = "force_refresh";
    static final String EXPIRES_IN_PROPERTY = "expires_in";
    static final String PHONE_INFO_PROPERTY = "phone_info";
    static final String ERROR_CODE_PROPERTY = "errcode";
    static final String ERROR_MESSAGE_PROPERTY = "errmsg";

    private WechatMiniProgramApiConstants() {
    }
}
