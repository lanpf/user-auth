package com.cloud.userauth.infrastructure.external.wechat.miniprogram.client.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WechatMiniProgramStableAccessTokenPayload(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("expires_in") Long expiresIn,
        @JsonProperty(WechatMiniProgramApiPayload.ERROR_CODE_PROPERTY) Integer errorCode,
        @JsonProperty(WechatMiniProgramApiPayload.ERROR_MESSAGE_PROPERTY) String errorMessage
) implements WechatMiniProgramApiPayload {
}
