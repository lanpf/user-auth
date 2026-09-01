package com.cloud.userauth.infrastructure.external.wechat.miniprogram.client;

import com.cloud.userauth.infrastructure.external.wechat.miniprogram.client.payload.WechatMiniProgramApiPayload;

/** 校验微信小程序远程 API 返回的原始 payload。 */
final class WechatMiniProgramApiPayloadValidator {
    private WechatMiniProgramApiPayloadValidator() {
    }

    static void validate(WechatMiniProgramApiPayload response) {
        if (response == null) {
            throw new WechatMiniProgramApiException("empty WeChat API response");
        }
        if (response.errorCode() != null && response.errorCode() != 0) {
            throw new WechatMiniProgramApiException(response.errorCode(), response.errorMessage());
        }
    }
}
