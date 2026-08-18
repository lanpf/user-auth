package com.cloud.userauth.application.login;

/**
 * 手机号完成业务认证并创建或恢复领域 Session 后的内部结果。
 *
 * <p>该结果不包含 Token；Token 由当前配置命中的登录 Provider 签发。</p>
 */
public record MobileOtpAuthenticationOutput(
        Long userId,
        Long authAccountId,
        String sessionId,
        boolean fromRegistrationFlow,
        boolean replayed
) {
}
