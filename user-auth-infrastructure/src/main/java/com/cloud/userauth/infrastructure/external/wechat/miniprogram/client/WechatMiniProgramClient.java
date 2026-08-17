package com.cloud.userauth.infrastructure.external.wechat.miniprogram.client;

public interface WechatMiniProgramClient {
    String exchangeLoginCode(String loginCode);

    String exchangePhoneCode(String phoneCode);
}
