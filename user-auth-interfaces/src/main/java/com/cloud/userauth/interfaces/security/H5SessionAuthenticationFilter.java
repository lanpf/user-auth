package com.cloud.userauth.interfaces.security;

import com.cloud.userauth.application.authentication.AuthenticatedSession;
import com.cloud.userauth.application.port.H5SessionStore;
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
public final class H5SessionAuthenticationFilter extends OncePerRequestFilter {
    private final H5SessionStore sessionStore;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            H5SessionCookie.read(request).ifPresent(credential ->
                    sessionStore.find(credential).ifPresent(resolved -> {
                        setAuthentication(resolved.authenticatedSession());
                        if (resolved.renewed()) {
                            H5SessionCookie.write(
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
                        SessionAuthenticationAuthorities.h5Session()));
    }
}
