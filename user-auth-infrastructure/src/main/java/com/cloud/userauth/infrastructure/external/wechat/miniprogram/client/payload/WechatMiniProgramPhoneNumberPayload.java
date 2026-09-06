package com.cloud.userauth.infrastructure.external.wechat.miniprogram.client.payload;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record WechatMiniProgramPhoneNumberPayload(
        @JsonProperty("phone_info") WechatPhoneInfo phoneInfo,
        @JsonProperty(WechatMiniProgramApiPayload.ERROR_CODE_PROPERTY) Integer errorCode,
        @JsonProperty(WechatMiniProgramApiPayload.ERROR_MESSAGE_PROPERTY) String errorMessage
) implements WechatMiniProgramApiPayload {
    public record WechatPhoneInfo(String phoneNumber, String purePhoneNumber, String countryCode) {
            @JsonCreator
            public WechatPhoneInfo(
                    @JsonProperty("phoneNumber") String phoneNumber,
                    @JsonProperty("purePhoneNumber") String purePhoneNumber,
                    @JsonProperty("countryCode") String countryCode
            ) {
                this.phoneNumber = phoneNumber;
                this.purePhoneNumber = purePhoneNumber;
                this.countryCode = countryCode;
            }
        }
}
