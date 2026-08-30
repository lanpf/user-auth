package com.cloud.userauth.infrastructure.external.wechat.miniprogram.client;

import com.fasterxml.jackson.annotation.JsonProperty;

interface WechatMiniProgramApiPayload {
    Integer errorCode();

    String errorMessage();
}

record WechatCode2SessionPayload(
        @JsonProperty(WechatMiniProgramApiConstants.OPEN_ID_PROPERTY) String openId,
        @JsonProperty(WechatMiniProgramApiConstants.SESSION_KEY_PROPERTY) String sessionKey,
        @JsonProperty(WechatMiniProgramApiConstants.UNION_ID_PROPERTY) String unionId,
        @JsonProperty(WechatMiniProgramApiConstants.ERROR_CODE_PROPERTY) Integer errorCode,
        @JsonProperty(WechatMiniProgramApiConstants.ERROR_MESSAGE_PROPERTY) String errorMessage
) implements WechatMiniProgramApiPayload {
}

record WechatStableAccessTokenRequest(
        @JsonProperty(WechatMiniProgramApiConstants.GRANT_TYPE_PARAMETER) String grantType,
        @JsonProperty(WechatMiniProgramApiConstants.APP_ID_PARAMETER) String appId,
        String secret,
        @JsonProperty(WechatMiniProgramApiConstants.FORCE_REFRESH_PROPERTY) boolean forceRefresh
) {
}

record WechatStableAccessTokenPayload(
        @JsonProperty(WechatMiniProgramApiConstants.ACCESS_TOKEN_PARAMETER) String accessToken,
        @JsonProperty(WechatMiniProgramApiConstants.EXPIRES_IN_PROPERTY) Long expiresIn,
        @JsonProperty(WechatMiniProgramApiConstants.ERROR_CODE_PROPERTY) Integer errorCode,
        @JsonProperty(WechatMiniProgramApiConstants.ERROR_MESSAGE_PROPERTY) String errorMessage
) implements WechatMiniProgramApiPayload {
}

record WechatPhoneNumberRequest(String code) {
}

record WechatPhoneNumberPayload(
        @JsonProperty(WechatMiniProgramApiConstants.PHONE_INFO_PROPERTY) WechatPhoneInfo phoneInfo,
        @JsonProperty(WechatMiniProgramApiConstants.ERROR_CODE_PROPERTY) Integer errorCode,
        @JsonProperty(WechatMiniProgramApiConstants.ERROR_MESSAGE_PROPERTY) String errorMessage
) implements WechatMiniProgramApiPayload {
}

record WechatPhoneInfo(
        String phoneNumber,
        String purePhoneNumber,
        String countryCode
) {
}
