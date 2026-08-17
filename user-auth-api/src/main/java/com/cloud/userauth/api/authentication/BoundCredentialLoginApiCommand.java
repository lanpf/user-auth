package com.cloud.userauth.api.authentication;

import com.cloud.framework.core.Request;
import jakarta.validation.constraints.NotBlank;

/** 已绑定外部 Credential 的无状态授权码登录或续期请求。 */
public record BoundCredentialLoginApiCommand(
        @NotBlank String issuer,
        @NotBlank String authorizationCode,
        @NotBlank String clientAppId,
        String clientPlatform,
        String clientVersion,
        @NotBlank String channelCode,
        String deviceId,
        String deviceType,
        String deviceName
) implements Request {
}
