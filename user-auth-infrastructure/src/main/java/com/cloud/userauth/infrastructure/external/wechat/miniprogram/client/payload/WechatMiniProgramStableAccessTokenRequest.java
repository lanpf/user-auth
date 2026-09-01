package com.cloud.userauth.infrastructure.external.wechat.miniprogram.client.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WechatMiniProgramStableAccessTokenRequest(
        @JsonProperty("grant_type") String grantType,
        @JsonProperty("appid") String appId,
        @JsonProperty("secret") String secret,
        @JsonProperty("force_refresh") boolean forceRefresh
) {
}
