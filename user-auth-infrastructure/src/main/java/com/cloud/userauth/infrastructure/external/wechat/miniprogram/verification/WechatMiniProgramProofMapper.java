package com.cloud.userauth.infrastructure.external.wechat.miniprogram.verification;

import com.cloud.userauth.domain.authentication.external.ProofParameters;

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
                loginCode(parameters),
                parameters.get(WechatMiniProgramProofParameters.PHONE_CODE));
    }

    private static String loginCode(Map<String, String> parameters) {
        String loginCode = parameters.get(WechatMiniProgramProofParameters.LOGIN_CODE);
        return loginCode != null
                ? loginCode
                : parameters.get(ProofParameters.AUTHORIZATION_CODE);
    }
}
