package com.cloud.userauth.interfaces.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.util.StringUtils;

/**
 * 浏览器 Cookie 的读取与安全属性写入策略。
 *
 * <p>逐请求验证由网关触发，滑动续期由 user-auth 在在线验证时完成；本类型仅服务三个时序点：签发（handoff 兑换）写入、
 * 宿主登出清除、浏览器会话结束读取（经网关 Cookie 白名单转发回来的原始凭据）。</p>
 */
public final class BrowserSessionCookie {
    private BrowserSessionCookie() {
    }

    public static final String NAME = "BROWSER_SESSION";

    public static Optional<String> read(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(cookie -> NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .filter(StringUtils::hasText)
                .findFirst();
    }

    public static void write(HttpServletResponse response, String credential, Duration maxAge) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie(credential, maxAge));
    }

    public static void clear(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie("", Duration.ZERO));
    }

    private static String cookie(String value, Duration maxAge) {
        return ResponseCookie.from(NAME, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAge)
                .build()
                .toString();
    }
}
