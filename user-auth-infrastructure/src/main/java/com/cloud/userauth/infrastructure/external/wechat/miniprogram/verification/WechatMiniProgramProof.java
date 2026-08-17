package com.cloud.userauth.infrastructure.external.wechat.miniprogram.verification;

import jakarta.validation.constraints.NotBlank;

public record WechatMiniProgramProof(
        @NotBlank String loginCode,
        String phoneCode
) {
}
