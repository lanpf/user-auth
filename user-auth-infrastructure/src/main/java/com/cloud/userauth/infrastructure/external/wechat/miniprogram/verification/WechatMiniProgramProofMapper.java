package com.cloud.userauth.infrastructure.external.wechat.miniprogram.verification;

import java.util.Map;
public final class WechatMiniProgramProofMapper {
    private WechatMiniProgramProofMapper() {
    }

    public static WechatMiniProgramProof toProof(
            Map<String, String> parameters
    ) {
        if (parameters == null) {
            return null;
        }
        return new WechatMiniProgramProof(
                authorizationCode(parameters),
                parameters.get(
                        WechatMiniProgramProofParameterNames.PHONE_CODE));
    }

    private static String authorizationCode(Map<String, String> parameters) {
        String authorizationCode = parameters.get(
                WechatMiniProgramProofParameterNames.AUTHORIZATION_CODE);
        return authorizationCode != null ? authorizationCode : parameters.get(
                WechatMiniProgramProofParameterNames.LOGIN_CODE);
    }
}
