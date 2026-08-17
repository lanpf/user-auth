package com.cloud.userauth.infrastructure.oauth2.sas.grant.mobileotp;

import com.cloud.userauth.application.common.ApplicationException;
import com.cloud.userauth.application.login.MobileAuthenticationCommandOutput;
import com.cloud.userauth.application.login.MobileOtpAuthenticationProcess;
import com.cloud.userauth.application.port.ClientRenewalPolicy;
import com.cloud.userauth.application.port.ClientRenewalPolicyResolver;
import com.cloud.userauth.domain.common.DomainException;
import com.cloud.userauth.infrastructure.oauth2.sas.grant.mobileotp.mapper.MobileOtpGrantRequestMapper;
import com.cloud.userauth.infrastructure.oauth2.sas.protocol.SasAuthorizationAttributes;
import com.cloud.userauth.infrastructure.oauth2.sas.protocol.SasTokenResponseParameters;
import com.cloud.userauth.infrastructure.oauth2.sas.scope.SasClientScopeResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.ClaimAccessor;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.util.StringUtils;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public final class MobileOtpGrantAuthenticationProvider implements AuthenticationProvider {
    private final MobileOtpAuthenticationProcess mobileOtpAuthenticationProcess;
    private final OAuth2AuthorizationService authorizationService;
    private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;
    private final SasClientScopeResolver clientScopeResolver;
    private final MobileOtpGrantRequestMapper requestMapper;
    private final ClientRenewalPolicyResolver renewalPolicyResolver;

    @Override
    public Authentication authenticate(Authentication authentication) {
        MobileOtpGrantAuthenticationToken grantAuthentication =
                (MobileOtpGrantAuthenticationToken) authentication;
        OAuth2ClientAuthenticationToken clientPrincipal = authenticatedClient(grantAuthentication);
        RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();
        if (registeredClient == null || !registeredClient.getAuthorizationGrantTypes().contains(MobileOtpGrantTypes.MOBILE_OTP)) {
            throw oauth2Exception(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT);
        }

        Set<String> authorizedScopes = authorizedScopes(grantAuthentication, registeredClient);
        MobileAuthenticationCommandOutput login = authenticateMobile(grantAuthentication);
        Authentication userPrincipal = UsernamePasswordAuthenticationToken.authenticated(
                String.valueOf(login.userId()), null, List.of());
        OAuth2Authorization.Builder authorizationBuilder = authorization(
                registeredClient, userPrincipal, login, authorizedScopes, grantAuthentication.request().clientAppId());
        DefaultOAuth2TokenContext.Builder tokenContextBuilder = tokenContext(
                registeredClient, userPrincipal, grantAuthentication, authorizedScopes, authorizationBuilder.build());

        OAuth2TokenContext accessTokenContext = tokenContextBuilder.tokenType(OAuth2TokenType.ACCESS_TOKEN).build();
        OAuth2AccessToken accessToken = accessToken(
                authorizationBuilder, tokenGenerator.generate(accessTokenContext), authorizedScopes);
        OAuth2RefreshToken refreshToken = refreshToken(
                authorizationBuilder, tokenContextBuilder, grantAuthentication.request().clientAppId());
        authorizationService.save(authorizationBuilder.build());

        Map<String, Object> responseParameters = buildResponseParameters(login);
        return new OAuth2AccessTokenAuthenticationToken(
                registeredClient, clientPrincipal, accessToken, refreshToken, responseParameters);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return MobileOtpGrantAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private MobileAuthenticationCommandOutput authenticateMobile(MobileOtpGrantAuthenticationToken grantAuthentication) {
        try {
            return mobileOtpAuthenticationProcess.authenticate(
                    requestMapper.toAuthenticationCommand(
                            grantAuthentication.request()));
        } catch (DomainException exception) {
            throw oauth2Exception(OAuth2ErrorCodes.INVALID_GRANT);
        } catch (ApplicationException exception) {
            throw oauth2Exception(OAuth2ErrorCodes.TEMPORARILY_UNAVAILABLE);
        } catch (RuntimeException exception) {
            throw oauth2Exception(OAuth2ErrorCodes.SERVER_ERROR);
        }
    }

    private OAuth2Authorization.Builder authorization(
            RegisteredClient client,
            Authentication userPrincipal,
            MobileAuthenticationCommandOutput login,
            Set<String> scopes,
            String clientAppId
    ) {
        return OAuth2Authorization.withRegisteredClient(client)
                .id(login.sessionId())
                .principalName(String.valueOf(login.userId()))
                .authorizationGrantType(MobileOtpGrantTypes.MOBILE_OTP)
                .authorizedScopes(scopes)
                .attribute(Principal.class.getName(), userPrincipal)
                .attribute(SasAuthorizationAttributes.USER_ID, login.userId())
                .attribute(SasAuthorizationAttributes.AUTH_ACCOUNT_ID, login.authAccountId())
                .attribute(SasAuthorizationAttributes.SESSION_ID, login.sessionId())
                .attribute(SasAuthorizationAttributes.CLIENT_APP_ID, clientAppId);
    }

    private DefaultOAuth2TokenContext.Builder tokenContext(
            RegisteredClient client,
            Authentication userPrincipal,
            MobileOtpGrantAuthenticationToken grantAuthentication,
            Set<String> scopes,
            OAuth2Authorization authorization
    ) {
        return DefaultOAuth2TokenContext.builder()
                .registeredClient(client)
                .principal(userPrincipal)
                .authorizationServerContext(AuthorizationServerContextHolder.getContext())
                .authorization(authorization)
                .authorizedScopes(scopes)
                .authorizationGrantType(MobileOtpGrantTypes.MOBILE_OTP)
                .authorizationGrant(grantAuthentication);
    }

    private OAuth2AccessToken accessToken(
            OAuth2Authorization.Builder authorizationBuilder,
            OAuth2Token generatedToken,
            Set<String> authorizedScopes
    ) {
        if (generatedToken == null) {
            throw oauth2Exception(OAuth2ErrorCodes.SERVER_ERROR);
        }
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                generatedToken.getTokenValue(),
                generatedToken.getIssuedAt(),
                generatedToken.getExpiresAt(),
                authorizedScopes);
        authorizationBuilder.token(accessToken, metadata -> {
            if (generatedToken instanceof ClaimAccessor claimAccessor) {
                metadata.put(
                        OAuth2Authorization.Token.CLAIMS_METADATA_NAME,
                        claimAccessor.getClaims());
            }
        });
        return accessToken;
    }

    private OAuth2RefreshToken refreshToken(
            OAuth2Authorization.Builder authorizationBuilder,
            DefaultOAuth2TokenContext.Builder tokenContextBuilder,
            String clientAppId
    ) {
        if (renewalPolicyResolver.resolve(clientAppId) != ClientRenewalPolicy.REFRESH_TOKEN_ROTATION) {
            return null;
        }
        OAuth2Token generated = tokenGenerator.generate(
                tokenContextBuilder.tokenType(OAuth2TokenType.REFRESH_TOKEN).build());
        if (!(generated instanceof OAuth2RefreshToken refreshToken)) {
            throw oauth2Exception(OAuth2ErrorCodes.SERVER_ERROR);
        }
        authorizationBuilder.refreshToken(refreshToken);
        return refreshToken;
    }

    private static OAuth2ClientAuthenticationToken authenticatedClient(
            MobileOtpGrantAuthenticationToken grantAuthentication
    ) {
        Object principal = grantAuthentication.getPrincipal();
        if (principal instanceof OAuth2ClientAuthenticationToken client && client.isAuthenticated()) {
            return client;
        }
        throw oauth2Exception(OAuth2ErrorCodes.INVALID_CLIENT);
    }

    private Set<String> authorizedScopes(
            MobileOtpGrantAuthenticationToken grantAuthentication,
            RegisteredClient client
    ) {
        Set<String> configuredScopes;
        try {
            configuredScopes = clientScopeResolver.resolve(
                    grantAuthentication.request().clientAppId());
        } catch (ApplicationException exception) {
            throw oauth2Exception(OAuth2ErrorCodes.INVALID_SCOPE);
        }
        if (!client.getScopes().containsAll(configuredScopes)) {
            throw oauth2Exception(OAuth2ErrorCodes.SERVER_ERROR);
        }
        String scope = grantAuthentication.request().scope();
        if (!StringUtils.hasText(scope)) {
            return configuredScopes;
        }
        Set<String> scopes;
        try {
            scopes = Set.of(StringUtils.tokenizeToStringArray(scope, " "));
        } catch (IllegalArgumentException exception) {
            throw oauth2Exception(OAuth2ErrorCodes.INVALID_SCOPE);
        }
        if (!configuredScopes.equals(scopes)) {
            throw oauth2Exception(OAuth2ErrorCodes.INVALID_SCOPE);
        }
        return configuredScopes;
    }

    private Map<String, Object> buildResponseParameters(MobileAuthenticationCommandOutput login) {
        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put(SasTokenResponseParameters.USER_ID, login.userId());
        parameters.put(SasTokenResponseParameters.AUTH_ACCOUNT_ID, login.authAccountId());
        parameters.put(SasTokenResponseParameters.SESSION_ID, login.sessionId());
        parameters.put(SasTokenResponseParameters.FROM_REGISTRATION_FLOW, login.fromRegistrationFlow());
        parameters.put(SasTokenResponseParameters.REPLAYED, login.replayed());

        return parameters;
    }

    private static OAuth2AuthenticationException oauth2Exception(String errorCode) {
        return new OAuth2AuthenticationException(new OAuth2Error(errorCode));
    }
}
