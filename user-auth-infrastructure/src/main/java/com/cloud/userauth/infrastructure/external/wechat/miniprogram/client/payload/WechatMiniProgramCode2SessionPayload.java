package com.cloud.userauth.infrastructure.external.wechat.miniprogram.client.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WechatMiniProgramCode2SessionPayload(
        @JsonProperty("openid") String openId,
        @JsonProperty("session_key") String sessionKey,
        @JsonProperty("unionid") String unionId,
        @JsonProperty(WechatMiniProgramApiPayload.ERROR_CODE_PROPERTY) Integer errorCode,
        @JsonProperty(WechatMiniProgramApiPayload.ERROR_MESSAGE_PROPERTY) String errorMessage
) implements WechatMiniProgramApiPayload {
}
