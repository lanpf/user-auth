package com.cloud.userauth.interfaces.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cloud.userauth.api.constants.JwtApiConstants;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;

class AuthenticatedSessionResolverTest {

    @Test
    void shouldResolveRequiredSessionClaims() {
        Jwt jwt = jwtBuilder()
                .claim(JwtApiConstants.USER_ID_CLAIM, 1001)
                .claim(JwtApiConstants.AUTH_ACCOUNT_ID_CLAIM, 2001L)
                .claim(JwtApiConstants.SESSION_ID_CLAIM, "session-1")
                .build();

        var authenticatedSession = AuthenticatedSessionResolver.resolve(jwt);

        assertEquals(1001L, authenticatedSession.userId());
        assertEquals(2001L, authenticatedSession.authAccountId());
        assertEquals("session-1", authenticatedSession.sessionId().value());
    }

    @Test
    void shouldRejectJwtWithoutRequiredSessionClaims() {
        Jwt jwt = jwtBuilder()
                .claim(JwtApiConstants.USER_ID_CLAIM, 1001L)
                .build();

        assertThrows(BadCredentialsException.class,
                () -> AuthenticatedSessionResolver.resolve(jwt));
    }

    @Test
    void shouldRejectUnsupportedPrincipal() {
        assertThrows(BadCredentialsException.class,
                () -> AuthenticatedSessionResolver.resolve("principal"));
    }

    private static Jwt.Builder jwtBuilder() {
        Instant now = Instant.now();
        return Jwt.withTokenValue("access-token")
                .header("alg", "RS256")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(300));
    }
}
