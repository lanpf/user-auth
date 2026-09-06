package com.cloud.userauth.api.authentication;

import com.cloud.framework.core.Request;
import jakarta.validation.constraints.NotBlank;

/** 验证浏览器会话凭据并取得当前认证主体的命令。 */
public record VerifyBrowserSessionApiCommand(
        @NotBlank String credential
) implements Request {
}
