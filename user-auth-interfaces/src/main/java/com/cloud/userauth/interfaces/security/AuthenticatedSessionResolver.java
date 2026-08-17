package com.cloud.userauth.interfaces.security;

import com.cloud.userauth.api.constants.JwtApiConstants;
import com.cloud.userauth.application.authentication.AuthenticatedSession;
import com.cloud.userauth.domain.authentication.session.SessionId;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.StringUtils;

/** 将不同认证协议的 Principal 归一为协议无关的已认证登录会话。 */
public final class AuthenticatedSessionResolver {
    private AuthenticatedSessionResolver() {
    }

    public static AuthenticatedSession resolve(Object principal) {
        if (principal instanceof AuthenticatedSession authenticatedSession) {
            return authenticatedSession;
        }
        if (principal instanceof Jwt jwt) {
            return new AuthenticatedSession(
                    requiredLongClaim(jwt, JwtApiConstants.USER_ID_CLAIM),
                    requiredLongClaim(jwt, JwtApiConstants.AUTH_ACCOUNT_ID_CLAIM),
                    new SessionId(requiredStringClaim(jwt, JwtApiConstants.SESSION_ID_CLAIM)));
        }
        throw new BadCredentialsException("Unsupported authenticated principal");
    }

    private static Long requiredLongClaim(Jwt jwt, String claimName) {
        Object claim = jwt.getClaim(claimName);
        if (claim instanceof Number number) {
            return number.longValue();
        }
        throw new BadCredentialsException("Required numeric JWT claim is missing: " + claimName);
    }

    private static String requiredStringClaim(Jwt jwt, String claimName) {
        String claim = jwt.getClaimAsString(claimName);
        if (StringUtils.hasText(claim)) {
            return claim;
        }
        throw new BadCredentialsException("Required string JWT claim is missing: " + claimName);
    }
}
