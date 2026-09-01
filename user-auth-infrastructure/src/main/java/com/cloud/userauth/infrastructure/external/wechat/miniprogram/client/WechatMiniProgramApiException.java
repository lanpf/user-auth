package com.cloud.userauth.infrastructure.external.wechat.miniprogram.client;

import lombok.Getter;

@Getter
final class WechatMiniProgramApiException extends RuntimeException {
    private static final int AUTHORIZATION_CODE_INVALID = 40029;
    private static final int ACCESS_TOKEN_INVALID = 40001;
    private static final int ACCESS_TOKEN_ILLEGAL = 40014;
    private static final int ACCESS_TOKEN_EXPIRED = 42001;
    private final Integer errorCode;

    WechatMiniProgramApiException(String errorMessage) {
        this(null, errorMessage);
    }

    WechatMiniProgramApiException(Integer errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
    }

    boolean isAuthorizationCodeInvalid() {
        return errorCode != null && errorCode == AUTHORIZATION_CODE_INVALID;
    }

    boolean isAccessTokenInvalid() {
        if (errorCode == null) {
            return false;
        }
        return errorCode == ACCESS_TOKEN_INVALID
                || errorCode == ACCESS_TOKEN_ILLEGAL
                || errorCode == ACCESS_TOKEN_EXPIRED;
    }
}
