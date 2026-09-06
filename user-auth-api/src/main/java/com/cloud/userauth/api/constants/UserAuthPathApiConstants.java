package com.cloud.userauth.api.constants;

public final class UserAuthPathApiConstants {
    public static final String SERVICE_NAME = "user-auth";
    public static final String API_BASE_PATH = "/api";

    public static final String API_CHALLENGES = API_BASE_PATH + "/challenges";
    public static final String API_LOGIN = API_BASE_PATH + "/login";
    public static final String API_CREDENTIALS = API_BASE_PATH + "/credentials";
    public static final String API_HANDOFFS = API_BASE_PATH + "/handoffs";

    public static final String API_LOGIN_MOBILE_OTP = API_LOGIN + "/mobile-otp";
    public static final String API_LOGIN_REFRESH = API_LOGIN + "/refresh";
    public static final String API_LOGIN_PARTNER = API_LOGIN + "/partner";
    public static final String API_LOGIN_PARTNER_TRUSTED_MOBILE = API_LOGIN_PARTNER + "/trusted-mobile";
    public static final String API_LOGIN_EXTERNAL = API_LOGIN + "/external";
    public static final String API_LOGIN_EXTERNAL_PROOF = API_LOGIN_EXTERNAL + "/proof";
    public static final String API_LOGIN_EXTERNAL_ATTEMPT = API_LOGIN_EXTERNAL + "/attempt";
    public static final String API_LOGIN_EXTERNAL_COMPLETE = API_LOGIN_EXTERNAL + "/complete";
    public static final String API_LOGIN_EXTERNAL_BOUND_CREDENTIAL = API_LOGIN_EXTERNAL + "/bound-credential";

    public static final String API_CREDENTIALS_BIND = API_CREDENTIALS + "/bind";
    public static final String API_LOGOUT = API_BASE_PATH + "/logout";
    public static final String API_HANDOFFS_EXCHANGE = API_HANDOFFS + "/exchange";

    public static final String API_BROWSER_SESSIONS = API_BASE_PATH + "/browser-sessions";
    public static final String API_BROWSER_SESSIONS_END = API_BROWSER_SESSIONS + "/end";

    public static final String INTERNAL_BASE_PATH = "/internal";
    public static final String INTERNAL_BROWSER_SESSIONS = INTERNAL_BASE_PATH + "/browser-sessions";
    public static final String INTERNAL_BROWSER_SESSIONS_VERIFY = INTERNAL_BROWSER_SESSIONS + "/verify";

    public static final String ADMIN_BASE_PATH = "/admin";
    public static final String ADMIN_AUTHORIZATION = ADMIN_BASE_PATH + "/authorization";
    public static final String ADMIN_AUTHORIZATION_PERMISSIONS = ADMIN_AUTHORIZATION + "/permissions";
    public static final String ADMIN_AUTHORIZATION_PERMISSIONS_ACTIVATE =
            ADMIN_AUTHORIZATION_PERMISSIONS + "/activate";
    public static final String ADMIN_AUTHORIZATION_PERMISSIONS_DISABLE =
            ADMIN_AUTHORIZATION_PERMISSIONS + "/disable";
    public static final String ADMIN_AUTHORIZATION_PERMISSIONS_QUERY =
            ADMIN_AUTHORIZATION_PERMISSIONS + "/query";
    public static final String ADMIN_AUTHORIZATION_PERMISSIONS_QUERY_PAGE =
            ADMIN_AUTHORIZATION_PERMISSIONS + "/query-page";
    public static final String ADMIN_AUTHORIZATION_ROLES = ADMIN_AUTHORIZATION + "/roles";
    public static final String ADMIN_AUTHORIZATION_ROLES_ACTIVATE = ADMIN_AUTHORIZATION_ROLES + "/activate";
    public static final String ADMIN_AUTHORIZATION_ROLES_DISABLE = ADMIN_AUTHORIZATION_ROLES + "/disable";
    public static final String ADMIN_AUTHORIZATION_ROLES_QUERY = ADMIN_AUTHORIZATION_ROLES + "/query";
    public static final String ADMIN_AUTHORIZATION_ROLES_PAGE = ADMIN_AUTHORIZATION_ROLES + "/page";
    public static final String ADMIN_AUTHORIZATION_CHANNEL_POLICIES =
            ADMIN_AUTHORIZATION + "/channel-policies";
    public static final String ADMIN_AUTHORIZATION_CHANNEL_POLICIES_ACTIVATE =
            ADMIN_AUTHORIZATION_CHANNEL_POLICIES + "/activate";
    public static final String ADMIN_AUTHORIZATION_CHANNEL_POLICIES_DISABLE =
            ADMIN_AUTHORIZATION_CHANNEL_POLICIES + "/disable";
    public static final String ADMIN_AUTHORIZATION_CHANNEL_POLICIES_RECONCILIATION =
            ADMIN_AUTHORIZATION_CHANNEL_POLICIES + "/reconciliation";
    public static final String ADMIN_AUTHORIZATION_CHANNEL_POLICIES_QUERY =
            ADMIN_AUTHORIZATION_CHANNEL_POLICIES + "/query";
    public static final String ADMIN_AUTHORIZATION_USERS_QUERY = ADMIN_AUTHORIZATION + "/users/query";

    private UserAuthPathApiConstants() {
    }
}
