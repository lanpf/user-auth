package com.cloud.userauth.infrastructure.external.wechat.miniprogram.client;

import lombok.Getter;

@Getter
final class WechatMiniProgramApiException extends RuntimeException {
    private final Integer errorCode;

    WechatMiniProgramApiException(Integer errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
    }
}
