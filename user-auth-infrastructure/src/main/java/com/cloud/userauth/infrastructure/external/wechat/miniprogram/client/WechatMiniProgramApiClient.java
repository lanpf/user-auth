package com.cloud.userauth.infrastructure.external.wechat.miniprogram.client;

import com.cloud.userauth.infrastructure.external.wechat.miniprogram.client.payload.WechatMiniProgramCode2SessionPayload;
import com.cloud.userauth.infrastructure.external.wechat.miniprogram.client.payload.WechatMiniProgramPhoneNumberPayload;
import com.cloud.userauth.infrastructure.external.wechat.miniprogram.client.payload.WechatMiniProgramStableAccessTokenPayload;

interface WechatMiniProgramApiClient {
    WechatMiniProgramStableAccessTokenPayload getStableAccessToken();

    WechatMiniProgramCode2SessionPayload exchangeLoginCode(String loginCode);

    WechatMiniProgramPhoneNumberPayload exchangePhoneCode(String accessToken, String phoneCode);
}
