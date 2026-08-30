package com.cloud.userauth.infrastructure.oauth2.sas.protocol;

public final class SasOAuth2RequestMessages {
    public static final String QUERY_PARAMETERS_NOT_ALLOWED =
            "OAuth2 Token request must not contain query parameters";
    public static final String FORM_URLENCODED_REQUIRED =
            "OAuth2 Token request must use application/x-www-form-urlencoded";
    public static final String INVALID_CONTENT_TYPE =
            "OAuth2 Token request contains an invalid Content-Type";
    public static final String INVALID = "OAuth2 parameter is invalid";
    public static final String INTEGER_REQUIRED = "OAuth2 parameter must be a valid integer";
    public static final String BOOLEAN_REQUIRED = "OAuth2 parameter must be a boolean";
    public static final String AT_MOST_ONCE = "OAuth2 parameter must occur at most once";

    public static String invalid(String name) {
        return INVALID + ": " + name;
    }
    public static String integerRequired(String name) {
        return INTEGER_REQUIRED + ": " + name;
    }

    public static String booleanRequired(String name) {
        return BOOLEAN_REQUIRED + ": " + name;
    }

    public static String atMostOnce(String name) {
        return AT_MOST_ONCE + ": " + name;
    }

    private SasOAuth2RequestMessages() {
    }
}
