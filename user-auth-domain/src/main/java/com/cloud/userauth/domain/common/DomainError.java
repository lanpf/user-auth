package com.cloud.userauth.domain.common;

import com.cloud.framework.core.error.BaseError;
import com.cloud.framework.core.error.ErrorCodeNamespace;
import com.cloud.framework.core.error.ErrorCodePrefix;
import com.cloud.framework.core.error.ErrorCodeRange;
import lombok.Getter;

@Getter
public enum DomainError implements BaseError {
    DOMAIN_ENTITY_ID_INVALID(0, "领域实体ID非法"),
    DOMAIN_EVENT_ID_REQUIRED(1, "领域事件ID不能为空"),
    DOMAIN_FIELD_REQUIRED(2, "领域字段不能为空"),

    AUTH_ACCOUNT_NOT_FOUND(100, "认证账号不存在"),
    AUTH_ACCOUNT_ALREADY_EXISTS(101, "认证账号已存在"),
    AUTH_ACCOUNT_DISABLED(102, "认证账号已禁用"),
    AUTH_ACCOUNT_LOCKED(103, "认证账号已锁定"),
    AUTH_ACCOUNT_MOBILE_REQUIRED(104, "手机号不能为空"),
    AUTH_ACCOUNT_MOBILE_INVALID(105, "手机号非法"),
    AUTH_ACCOUNT_CREDENTIAL_NOT_FOUND(106, "凭证不存在"),
    AUTH_ACCOUNT_CREDENTIAL_ALREADY_EXISTS(107, "凭证已存在"),
    AUTH_ACCOUNT_CREDENTIAL_DISABLED(108, "凭证已禁用"),
    AUTH_ACCOUNT_MOBILE_CREDENTIAL_REQUIRED(109, "认证账号必须拥有有效手机号凭证"),
    AUTH_ACCOUNT_CREDENTIAL_REVOKED(110, "凭证已撤销"),
    AUTH_ACCOUNT_EXTERNAL_IDENTITY_INVALID(111, "外部身份非法"),
    AUTH_ACCOUNT_EXTERNAL_MOBILE_UNTRUSTED(112, "外部手机号不可信"),

    AUTH_CHALLENGE_NOT_FOUND(150, "认证挑战不存在"),
    AUTH_CHALLENGE_ALREADY_EXISTS(151, "认证挑战已存在"),
    AUTH_CHALLENGE_TYPE_MISMATCH(152, "认证挑战类型不匹配"),
    AUTH_CHALLENGE_SCENE_MISMATCH(153, "认证挑战场景不匹配"),
    AUTH_CHALLENGE_EXPIRED(154, "认证挑战已过期"),
    AUTH_CHALLENGE_INVALID(155, "认证挑战答案错误"),
    AUTH_CHALLENGE_RETRY_EXCEEDED(156, "认证挑战尝试次数过多"),
    AUTH_CHALLENGE_CONSUMED(157, "认证挑战已被其他操作消费"),

    LOGIN_ATTEMPT_NOT_FOUND(200, "登录尝试不存在"),
    LOGIN_ATTEMPT_ALREADY_EXISTS(201, "登录尝试已存在"),
    LOGIN_ATTEMPT_EXPIRED(202, "登录尝试已过期"),
    LOGIN_ATTEMPT_COMPLETED(203, "登录尝试已完成"),

    LOGIN_SESSION_NOT_FOUND(250, "登录会话不存在"),
    LOGIN_SESSION_ALREADY_EXISTS(251, "登录会话已存在"),
    LOGIN_SESSION_INACTIVE(252, "登录会话不可用"),
    LOGIN_SESSION_TOKEN_NOT_FOUND(253, "令牌不存在"),
    LOGIN_SESSION_TOKEN_ALREADY_EXISTS(254, "令牌已存在"),
    LOGIN_SESSION_TOKEN_INACTIVE(255, "令牌不可用"),

    ROLE_NOT_FOUND(300, "角色不存在"),
    ROLE_ALREADY_EXISTS(301, "角色已存在"),
    ROLE_DISABLED(302, "角色已禁用"),
    ROLE_PERMISSION_DENIED(303, "权限不足"),
    ROLE_CODE_INVALID(304, "角色编码非法"),

    USER_ROLE_GRANT_NOT_FOUND(350, "角色授权不存在"),
    USER_ROLE_GRANT_ALREADY_EXISTS(351, "角色授权已存在"),

    PERMISSION_NOT_FOUND(400, "功能权限不存在"),
    PERMISSION_ALREADY_EXISTS(401, "功能权限已存在"),
    PERMISSION_DISABLED(402, "功能权限已停用"),
    PERMISSION_CODE_INVALID(403, "功能权限编码非法"),

    USER_PERMISSION_GRANT_NOT_FOUND(450, "直接权限授权不存在"),
    USER_PERMISSION_GRANT_ALREADY_EXISTS(451, "直接权限授权已存在"),

    CHANNEL_AUTHORIZATION_POLICY_NOT_FOUND(500, "渠道授权策略不存在"),
    CHANNEL_AUTHORIZATION_POLICY_ALREADY_EXISTS(501, "渠道授权策略已存在"),
    CHANNEL_AUTHORIZATION_POLICY_VERSION_CONFLICT(502, "渠道授权策略版本冲突");

    private final int localCode;
    private final String message;

    DomainError(int localCode, String message) {
        ErrorCodeRange.assertLocalCode(localCode);
        this.localCode = localCode;
        this.message = message;
    }

    @Override
    public ErrorCodePrefix getPrefix() {
        return new ErrorCodePrefix(ErrorCodeNamespace.BIZ, 0);
    }
}
