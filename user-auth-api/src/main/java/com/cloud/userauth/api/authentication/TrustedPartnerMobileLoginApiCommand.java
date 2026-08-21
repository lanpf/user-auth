package com.cloud.userauth.api.authentication;

import com.cloud.framework.core.Request;
import jakarta.validation.constraints.NotBlank;

/** 网关已验证合作方可信手机号断言后的直连登录命令。 */
public record TrustedPartnerMobileLoginApiCommand(
        @NotBlank String partnerCode,
        @NotBlank String partnerBizId,
        @NotBlank String mobile,
        String deviceId,
        String deviceType,
        String deviceName,
        @NotBlank String clientAppId,
        String clientPlatform,
        String clientVersion,
        @NotBlank String channelCode
) implements Request {
}
