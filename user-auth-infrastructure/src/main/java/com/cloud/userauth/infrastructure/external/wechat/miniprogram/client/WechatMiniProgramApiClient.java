package com.cloud.userauth.infrastructure.external.wechat.miniprogram.client;

interface WechatMiniProgramApiClient {
    WechatStableAccessTokenPayload getStableAccessToken();

    WechatCode2SessionPayload exchangeLoginCode(String loginCode);

    WechatPhoneNumberPayload exchangePhoneCode(String accessToken, String phoneCode);
}
