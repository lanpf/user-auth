package com.cloud.userauth.interfaces.rest;

public final class UserAuthRestPaths {
    public static final String API_BASE_PATH = "/api/user-auth";
    public static final String API_AUTH_CHALLENGES = API_BASE_PATH + "/auth-challenges";
    public static final String API_LOGIN_MOBILE_OTP = API_BASE_PATH + "/login/mobile-otp";
    public static final String API_LOGIN_REFRESH = API_BASE_PATH + "/login/refresh";
    public static final String API_LOGIN_EXTERNAL_ATTEMPTS =
            API_BASE_PATH + "/login/external/attempts";
    public static final String API_LOGIN_EXTERNAL = API_BASE_PATH + "/login/external";
    public static final String API_LOGIN_EXTERNAL_TRUSTED_MOBILE =
            API_BASE_PATH + "/login/external/trusted-mobile";
    public static final String API_LOGIN_EXTERNAL_BOUND_CREDENTIAL =
            API_BASE_PATH + "/login/external/bound";
    public static final String API_CREDENTIALS_EXTERNAL_BIND = API_BASE_PATH + "/credentials/external/bind";
    public static final String API_LOGOUT = API_BASE_PATH + "/logout";
    public static final String API_WEB_VIEW_HANDOFFS = API_BASE_PATH + "/web-view-handoffs";
    public static final String API_WEB_VIEW_HANDOFFS_EXCHANGE =
            API_WEB_VIEW_HANDOFFS + "/exchange";

    public static final String ADMIN_BASE_PATH = "/admin/user-auth";
    public static final String ADMIN_AUTHORIZATION = ADMIN_BASE_PATH + "/authorization";
    public static final String ADMIN_AUTHORIZATION_PERMISSIONS =
            ADMIN_AUTHORIZATION + "/permissions";
    public static final String ADMIN_AUTHORIZATION_PERMISSIONS_ACTIVATE =
            ADMIN_AUTHORIZATION_PERMISSIONS + "/activate";
    public static final String ADMIN_AUTHORIZATION_PERMISSIONS_DISABLE =
            ADMIN_AUTHORIZATION_PERMISSIONS + "/disable";
    public static final String ADMIN_AUTHORIZATION_PERMISSIONS_QUERY =
            ADMIN_AUTHORIZATION_PERMISSIONS + "/query";
    public static final String ADMIN_AUTHORIZATION_PERMISSIONS_QUERY_PAGE =
            ADMIN_AUTHORIZATION_PERMISSIONS + "/query-page";
    public static final String ADMIN_AUTHORIZATION_ROLES = ADMIN_AUTHORIZATION + "/roles";
    public static final String ADMIN_AUTHORIZATION_ROLES_ACTIVATE =
            ADMIN_AUTHORIZATION_ROLES + "/activate";
    public static final String ADMIN_AUTHORIZATION_ROLES_DISABLE =
            ADMIN_AUTHORIZATION_ROLES + "/disable";
    public static final String ADMIN_AUTHORIZATION_ROLES_QUERY =
            ADMIN_AUTHORIZATION_ROLES + "/query";
    public static final String ADMIN_AUTHORIZATION_ROLES_QUERY_PAGE =
            ADMIN_AUTHORIZATION_ROLES + "/query-page";
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
    public static final String ADMIN_AUTHORIZATION_USERS_QUERY =
            ADMIN_AUTHORIZATION + "/users/query";

    private UserAuthRestPaths() {
    }
}
