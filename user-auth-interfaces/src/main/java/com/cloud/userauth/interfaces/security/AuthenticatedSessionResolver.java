package com.cloud.userauth.interfaces.security;

import com.cloud.userauth.api.constants.AccessTokenClaimApiConstants;
import com.cloud.userauth.application.authentication.AuthenticatedSession;
import com.cloud.userauth.domain.authentication.session.SessionId;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
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
                    requiredLong(jwt.getClaim(AccessTokenClaimApiConstants.USER_ID_CLAIM),
                            AccessTokenClaimApiConstants.USER_ID_CLAIM),
                    requiredLong(jwt.getClaim(AccessTokenClaimApiConstants.AUTH_ACCOUNT_ID_CLAIM),
                            AccessTokenClaimApiConstants.AUTH_ACCOUNT_ID_CLAIM),
                    new SessionId(requiredString(
                            jwt.getClaim(AccessTokenClaimApiConstants.SESSION_ID_CLAIM),
                            AccessTokenClaimApiConstants.SESSION_ID_CLAIM)));
        }
        if (principal instanceof OAuth2AuthenticatedPrincipal authenticatedPrincipal) {
            return new AuthenticatedSession(
                    requiredLong(
                            authenticatedPrincipal.getAttribute(
                                    AccessTokenClaimApiConstants.USER_ID_CLAIM),
                            AccessTokenClaimApiConstants.USER_ID_CLAIM),
                    requiredLong(
                            authenticatedPrincipal.getAttribute(
                                    AccessTokenClaimApiConstants.AUTH_ACCOUNT_ID_CLAIM),
                            AccessTokenClaimApiConstants.AUTH_ACCOUNT_ID_CLAIM),
                    new SessionId(requiredString(
                            authenticatedPrincipal.getAttribute(
                                    AccessTokenClaimApiConstants.SESSION_ID_CLAIM),
                            AccessTokenClaimApiConstants.SESSION_ID_CLAIM)));
        }
        throw new BadCredentialsException("Unsupported authenticated principal");
    }

    private static Long requiredLong(Object claim, String claimName) {
        if (claim instanceof Number number) {
            return number.longValue();
        }
        throw new BadCredentialsException(
                "Required numeric access token claim is missing: " + claimName);
    }

    private static String requiredString(Object claim, String claimName) {
        if (claim instanceof String value && StringUtils.hasText(value)) {
            return value;
        }
        throw new BadCredentialsException(
                "Required string access token claim is missing: " + claimName);
    }
}
