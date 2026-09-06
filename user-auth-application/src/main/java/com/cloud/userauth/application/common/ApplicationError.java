package com.cloud.userauth.application.common;

import com.cloud.framework.core.error.BaseError;
import com.cloud.framework.core.error.ErrorCodeNamespace;
import com.cloud.framework.core.error.ErrorCodePrefix;
import com.cloud.framework.core.error.ErrorCodeRange;
import lombok.Getter;

@Getter
public enum ApplicationError implements BaseError {
    APP_USER_INITIALIZATION_FAILED(600, "用户资料初始化失败"),
    APP_REGISTRATION_PROCESS_NOT_FOUND(601, "注册流程不存在"),
    APP_REGISTRATION_PROCESS_ALREADY_EXISTS(602, "注册流程已存在"),
    APP_REGISTRATION_PROCESS_NOT_READY(603, "注册流程尚未完成用户资料初始化"),
    APP_REGISTRATION_PROCESS_TERMINATED(604, "注册流程已终止"),
    APP_MOBILE_LOGIN_IN_PROGRESS(605, "该手机号的登录正在处理中"),
    APP_AUTH_CHALLENGE_ISSUE_IN_PROGRESS(606, "认证挑战正在签发中"),
    APP_LOGIN_REJECTED(608, "登录失败"),
    APP_TOKEN_ISSUE_FAILED(609, "登录令牌签发失败"),
    APP_INTERNAL_TOKEN_REQUEST_LIMIT_REACHED(610, "内部令牌请求繁忙"),
    APP_LOGIN_CLIENT_NOT_ALLOWED(611, "登录客户端未授权"),
    APP_EXTERNAL_IDENTITY_ISSUER_NOT_SUPPORTED(612, "不支持该外部身份来源"),
    APP_SESSION_HANDOFF_TICKET_INVALID(613, "会话交接票据无效或已过期"),
    APP_CLIENT_RENEWAL_POLICY_NOT_ALLOWED(615, "客户端不允许使用 Refresh Token 续期"),
    APP_REFRESH_TOKEN_FAILED(616, "Refresh Token 续期失败"),
    APP_BROWSER_SESSION_INVALID(617, "浏览器会话无效或不属于当前登录会话");

    private final int localCode;
    private final String message;

    ApplicationError(int localCode, String message) {
        ErrorCodeRange.assertLocalCode(localCode);
        this.localCode = localCode;
        this.message = message;
    }

    @Override
    public ErrorCodePrefix getPrefix() {
        return new ErrorCodePrefix(ErrorCodeNamespace.BIZ, 0);
    }
}
