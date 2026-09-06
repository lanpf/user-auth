package com.cloud.userauth.api.authentication;

import com.cloud.framework.core.Request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** 结束当前浏览器会话的命令；credential 为待结束的 Browser Session 凭据。 */
public record EndBrowserSessionApiCommand(
        @NotNull Long authenticatedUserId,
        @NotBlank String sessionId,
        @NotBlank String credential
) implements Request {
}
