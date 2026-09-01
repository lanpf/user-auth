package com.cloud.userauth.infrastructure.external.wechat.miniprogram.client.payload;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

public record WechatMiniProgramPhoneNumberPayload(
        @JsonProperty("phone_info") WechatPhoneInfo phoneInfo,
        @JsonProperty(WechatMiniProgramApiPayload.ERROR_CODE_PROPERTY) Integer errorCode,
        @JsonProperty(WechatMiniProgramApiPayload.ERROR_MESSAGE_PROPERTY) String errorMessage
) implements WechatMiniProgramApiPayload {
    @Getter
    public static final class WechatPhoneInfo {
        private final String phoneNumber;
        private final String purePhoneNumber;
        private final String countryCode;

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
