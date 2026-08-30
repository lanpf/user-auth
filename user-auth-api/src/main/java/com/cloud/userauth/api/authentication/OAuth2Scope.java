package com.cloud.userauth.api.authentication;

import lombok.RequiredArgsConstructor;

/** user-auth 支持的 OAuth2 原始 scope；不包含 Spring Security 的 authority 前缀。 */
@RequiredArgsConstructor
public enum OAuth2Scope {
    APP("app"),
    ADMIN("admin");

    private final String value;

    public String value() {
        return value;
    }
}
