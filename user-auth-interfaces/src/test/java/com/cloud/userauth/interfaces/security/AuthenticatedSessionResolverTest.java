package com.cloud.userauth.interfaces.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cloud.userauth.api.constants.AccessTokenClaimApiConstants;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;

class AuthenticatedSessionResolverTest {

    @Test
    void shouldResolveRequiredSessionClaims() {
        Jwt jwt = jwtBuilder()
                .claim(AccessTokenClaimApiConstants.USER_ID_CLAIM, 1001)
                .claim(AccessTokenClaimApiConstants.AUTH_ACCOUNT_ID_CLAIM, 2001L)
                .claim(AccessTokenClaimApiConstants.SESSION_ID_CLAIM, "session-1")
                .build();

        var authenticatedSession = AuthenticatedSessionResolver.resolve(jwt);

        assertEquals(1001L, authenticatedSession.userId());
        assertEquals(2001L, authenticatedSession.authAccountId());
        assertEquals("session-1", authenticatedSession.sessionId().value());
    }

    @Test
    void shouldRejectJwtWithoutRequiredSessionClaims() {
        Jwt jwt = jwtBuilder()
                .claim(AccessTokenClaimApiConstants.USER_ID_CLAIM, 1001L)
                .build();

        assertThrows(BadCredentialsException.class,
                () -> AuthenticatedSessionResolver.resolve(jwt));
    }

    @Test
    void shouldResolveRequiredReferenceAccessTokenClaims() {
        DefaultOAuth2AuthenticatedPrincipal principal = new DefaultOAuth2AuthenticatedPrincipal(
                "1001",
                Map.of(
                        AccessTokenClaimApiConstants.USER_ID_CLAIM, 1001,
                        AccessTokenClaimApiConstants.AUTH_ACCOUNT_ID_CLAIM, 2001L,
                        AccessTokenClaimApiConstants.SESSION_ID_CLAIM, "session-1"),
                Set.of());

        var authenticatedSession = AuthenticatedSessionResolver.resolve(principal);

        assertEquals(1001L, authenticatedSession.userId());
        assertEquals(2001L, authenticatedSession.authAccountId());
        assertEquals("session-1", authenticatedSession.sessionId().value());
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
