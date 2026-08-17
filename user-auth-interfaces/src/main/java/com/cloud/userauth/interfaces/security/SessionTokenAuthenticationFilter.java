package com.cloud.userauth.interfaces.security;

import com.cloud.userauth.application.authentication.AuthenticatedSession;
import com.cloud.userauth.application.port.SessionTokenStore;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@RequiredArgsConstructor
public final class SessionTokenAuthenticationFilter extends OncePerRequestFilter {
    public static final String SESSION_TOKEN_HEADER = "X-Session-Token";

    private final SessionTokenStore sessionTokenStore;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            sessionToken(request)
                    .flatMap(sessionTokenStore::findAuthenticatedSession)
                    .ifPresent(this::setAuthentication);
        }
        filterChain.doFilter(request, response);
    }

    private void setAuthentication(AuthenticatedSession authenticatedSession) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        authenticatedSession,
                        null,
                        SessionAuthenticationAuthorities.hostSession()));
    }

    private static Optional<String> sessionToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(SESSION_TOKEN_HEADER))
                .filter(token -> !token.isBlank());
    }
}
