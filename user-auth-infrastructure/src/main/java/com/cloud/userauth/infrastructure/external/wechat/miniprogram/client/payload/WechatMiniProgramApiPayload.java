package com.cloud.userauth.infrastructure.external.wechat.miniprogram.client.payload;


public interface WechatMiniProgramApiPayload {
    String ERROR_CODE_PROPERTY = "errcode";
    String ERROR_MESSAGE_PROPERTY = "errmsg";

    Integer errorCode();

    String errorMessage();

}
