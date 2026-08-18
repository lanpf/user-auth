package com.cloud.userauth.interfaces.security;

import com.cloud.userauth.application.authentication.AuthenticatedSession;
import com.cloud.userauth.application.port.BrowserSessionStore;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public final class BrowserSessionAuthenticationFilter extends OncePerRequestFilter {
    private final BrowserSessionStore browserSessionStore;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            BrowserSessionCookie.read(request).ifPresent(credential ->
                    browserSessionStore.find(credential).ifPresent(resolved -> {
                        setAuthentication(resolved.authenticatedSession());
                        if (resolved.renewed()) {
                            BrowserSessionCookie.write(
                                    response,
                                    credential,
                                    resolved.remainingIdleTtl());
                        }
                    }));
        }
        filterChain.doFilter(request, response);
    }

    private static void setAuthentication(AuthenticatedSession authenticatedSession) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        authenticatedSession,
                        null,
                        SessionAuthenticationAuthorities.browserSession()));
    }
}
