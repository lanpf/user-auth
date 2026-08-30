package com.cloud.userauth.interfaces.security;

import com.cloud.framework.core.RequestHeader;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/** 将可信网关写入的认证会话 Header 转换为接口访问权限。 */
public final class ForwardedAuthenticatedSessionFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String userId = request.getHeader(RequestHeader.USER_ID);
        String sessionId = request.getHeader(RequestHeader.SESSION_ID);
        if (StringUtils.hasText(userId) && StringUtils.hasText(sessionId)) {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(
                            new ForwardedAuthenticatedSession(userId, sessionId),
                            null,
                            SessionAuthenticationAuthority.HOST_SESSION.authorities()));
        }
        filterChain.doFilter(request, response);
    }

    public record ForwardedAuthenticatedSession(String userId, String sessionId) {
    }
}
