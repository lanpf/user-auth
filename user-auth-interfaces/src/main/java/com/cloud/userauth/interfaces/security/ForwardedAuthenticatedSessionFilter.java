package com.cloud.userauth.interfaces.security;

import com.cloud.framework.core.RequestHeader;
import com.cloud.framework.core.SubjectType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/** 将可信网关写入的认证会话上下文转换为接口访问权限；缺失或未知的主体类型不授予任何权限。 */
public final class ForwardedAuthenticatedSessionFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String subjectType = request.getHeader(RequestHeader.SUBJECT_TYPE);
        String userId = request.getHeader(RequestHeader.USER_ID);
        String sessionId = request.getHeader(RequestHeader.SESSION_ID);
        if (StringUtils.hasText(userId) && StringUtils.hasText(sessionId)) {
            authority(subjectType).ifPresent(authority -> SecurityContextHolder.getContext()
                    .setAuthentication(new UsernamePasswordAuthenticationToken(
                            new ForwardedAuthenticatedSession(userId, sessionId),
                            null,
                            authority.authorities())));
        }
        filterChain.doFilter(request, response);
    }

    private static Optional<SessionAuthenticationAuthority> authority(String subjectType) {
        if (SubjectType.HOST_SESSION.name().equals(subjectType)) {
            return Optional.of(SessionAuthenticationAuthority.HOST_SESSION);
        }
        if (SubjectType.BROWSER_SESSION.name().equals(subjectType)) {
            return Optional.of(SessionAuthenticationAuthority.BROWSER_SESSION);
        }
        return Optional.empty();
    }

    public record ForwardedAuthenticatedSession(String userId, String sessionId) {
    }
}
