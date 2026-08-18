package com.cloud.userauth.api.authentication;

/** user-auth 支持的 OAuth2 原始 scope；不包含 Spring Security 的 authority 前缀。 */
public enum OAuth2Scope {
    APP("app"),
    ADMIN("admin");

    private final String value;

    OAuth2Scope(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
