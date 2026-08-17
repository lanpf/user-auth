package com.cloud.userauth.infrastructure.external.wechat.miniprogram.client;

interface WechatMiniProgramApiPayload {
    Integer errorCode();

    String errorMessage();
}

record WechatCode2SessionPayload(
        String openId,
        String sessionKey,
        String unionId,
        Integer errorCode,
        String errorMessage
) implements WechatMiniProgramApiPayload {
}

record WechatStableAccessTokenRequest(
        String grantType,
        String appId,
        String secret,
        boolean forceRefresh
) {
}

record WechatStableAccessTokenPayload(
        String accessToken,
        Long expiresIn,
        Integer errorCode,
        String errorMessage
) implements WechatMiniProgramApiPayload {
}

record WechatPhoneNumberRequest(String code) {
}

record WechatPhoneNumberPayload(
        WechatPhoneInfo phoneInfo,
        Integer errorCode,
        String errorMessage
) implements WechatMiniProgramApiPayload {
}

record WechatPhoneInfo(
        String phoneNumber,
        String purePhoneNumber,
        String countryCode
) {
}
