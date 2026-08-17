package com.cloud.userauth.infrastructure.oauth2.sas.grant.mobileotp;

/** mobile_otp Grant 的自定义协议参数名。 */
public final class MobileOtpGrantParameterNames {
    public static final String CHALLENGE_ID = "challenge_id";
    public static final String CODE = "code";
    public static final String DEVICE_ID = "device_id";
    public static final String DEVICE_TYPE = "device_type";
    public static final String DEVICE_NAME = "device_name";
    public static final String CLIENT_APP_ID = "client_app_id";
    public static final String CLIENT_PLATFORM = "client_platform";
    public static final String CLIENT_VERSION = "client_version";
    public static final String CHANNEL_CODE = "channel_code";

    private MobileOtpGrantParameterNames() {
    }
}
