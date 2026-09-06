package com.cloud.userauth.api.authentication;

/** 浏览器会话在线验证输出；未通过验证时仅 verified 为 false。 */
public record VerifyBrowserSessionApiCommandOutput(
        boolean verified,
        Long userId,
        String sessionId,
        Long remainingIdleTtlSeconds
) {

    public static VerifyBrowserSessionApiCommandOutput verified(
            Long userId,
            String sessionId,
            Long remainingIdleTtlSeconds
    ) {
        return new VerifyBrowserSessionApiCommandOutput(
                true, userId, sessionId, remainingIdleTtlSeconds);
    }

    public static VerifyBrowserSessionApiCommandOutput unverified() {
        return new VerifyBrowserSessionApiCommandOutput(false, null, null, null);
    }
}
