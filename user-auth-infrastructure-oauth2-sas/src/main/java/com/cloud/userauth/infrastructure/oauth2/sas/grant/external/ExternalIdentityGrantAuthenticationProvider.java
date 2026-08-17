package com.cloud.userauth.infrastructure.oauth2.sas.grant.external;

import com.cloud.userauth.application.common.ApplicationException;
import com.cloud.userauth.application.login.external.ExternalAuthenticationCommandOutput;
import com.cloud.userauth.application.login.external.ExternalAuthenticationProcess;
import com.cloud.userauth.application.port.ClientRenewalPolicy;
import com.cloud.userauth.application.port.ClientRenewalPolicyResolver;
import com.cloud.userauth.domain.common.DomainException;
import com.cloud.userauth.infrastructure.oauth2.sas.grant.external.mapper.ExternalIdentityGrantRequestMapper;
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
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.util.StringUtils;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public final class ExternalIdentityGrantAuthenticationProvider implements AuthenticationProvider {
    private final ExternalAuthenticationProcess authenticationProcess;
    private final OAuth2AuthorizationService authorizationService;
    private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;
    private final SasClientScopeResolver clientScopeResolver;
    private final ExternalIdentityGrantRequestMapper requestMapper;
    private final ClientRenewalPolicyResolver renewalPolicyResolver;

    @Override
    public Authentication authenticate(Authentication authentication) {
        ExternalIdentityGrantAuthenticationToken grant =
                (ExternalIdentityGrantAuthenticationToken) authentication;
        OAuth2ClientAuthenticationToken clientPrincipal = authenticatedClient(grant);
        RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();
        if (registeredClient == null
                || !registeredClient.getAuthorizationGrantTypes()
                .contains(ExternalIdentityGrantTypes.EXTERNAL_IDENTITY)) {
            throw oauth2(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT);
        }
        Set<String> scopes = authorizedScopes(grant, registeredClient);
        ExternalAuthenticationCommandOutput login = authenticateExternal(grant);
        Authentication userPrincipal = UsernamePasswordAuthenticationToken.authenticated(
                String.valueOf(login.userId()), null, List.of());
        OAuth2Authorization.Builder authorization = authorization(
                registeredClient, userPrincipal, login, scopes, grant.request().clientAppId());
        DefaultOAuth2TokenContext.Builder tokenContext = tokenContext(
                registeredClient, userPrincipal, grant, scopes, authorization.build());
        OAuth2AccessToken accessToken = accessToken(
                authorization,
                tokenGenerator.generate(tokenContext.tokenType(OAuth2TokenType.ACCESS_TOKEN).build()),
                scopes);
        OAuth2RefreshToken refreshToken = refreshToken(
                authorization, tokenContext, grant.request().clientAppId());
        authorizationService.save(authorization.build());
        return new OAuth2AccessTokenAuthenticationToken(
                registeredClient,
                clientPrincipal,
                accessToken,
                refreshToken,
                responseParameters(login));
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return ExternalIdentityGrantAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private ExternalAuthenticationCommandOutput authenticateExternal(
            ExternalIdentityGrantAuthenticationToken grant
    ) {
        try {
            return authenticationProcess.authenticate(
                    requestMapper.toAuthenticationCommand(grant.request()));
        } catch (DomainException exception) {
            throw oauth2(OAuth2ErrorCodes.INVALID_GRANT);
        } catch (ApplicationException exception) {
            throw oauth2(OAuth2ErrorCodes.TEMPORARILY_UNAVAILABLE);
        } catch (RuntimeException exception) {
            throw oauth2(OAuth2ErrorCodes.SERVER_ERROR);
        }
    }

    private OAuth2Authorization.Builder authorization(
            RegisteredClient client,
            Authentication userPrincipal,
            ExternalAuthenticationCommandOutput login,
            Set<String> scopes,
            String clientAppId
    ) {
        return OAuth2Authorization.withRegisteredClient(client)
                .id(login.sessionId())
                .principalName(String.valueOf(login.userId()))
                .authorizationGrantType(ExternalIdentityGrantTypes.EXTERNAL_IDENTITY)
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
            ExternalIdentityGrantAuthenticationToken grant,
            Set<String> scopes,
            OAuth2Authorization authorization
    ) {
        return DefaultOAuth2TokenContext.builder()
                .registeredClient(client)
                .principal(userPrincipal)
                .authorizationServerContext(AuthorizationServerContextHolder.getContext())
                .authorization(authorization)
                .authorizedScopes(scopes)
                .authorizationGrantType(ExternalIdentityGrantTypes.EXTERNAL_IDENTITY)
                .authorizationGrant(grant);
    }

    private OAuth2AccessToken accessToken(
            OAuth2Authorization.Builder authorization,
            OAuth2Token generated,
            Set<String> scopes
    ) {
        if (generated == null) {
            throw oauth2(OAuth2ErrorCodes.SERVER_ERROR);
        }
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                generated.getTokenValue(),
                generated.getIssuedAt(),
                generated.getExpiresAt(),
                scopes);
        authorization.token(accessToken, metadata -> {
            if (generated instanceof ClaimAccessor accessor) {
                metadata.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME, accessor.getClaims());
            }
        });
        return accessToken;
    }

    private OAuth2RefreshToken refreshToken(
            OAuth2Authorization.Builder authorization,
            DefaultOAuth2TokenContext.Builder tokenContext,
            String clientAppId
    ) {
        if (renewalPolicyResolver.resolve(clientAppId) != ClientRenewalPolicy.REFRESH_TOKEN_ROTATION) {
            return null;
        }
        OAuth2Token generated = tokenGenerator.generate(
                tokenContext.tokenType(OAuth2TokenType.REFRESH_TOKEN).build());
        if (!(generated instanceof OAuth2RefreshToken refreshToken)) {
            throw oauth2(OAuth2ErrorCodes.SERVER_ERROR);
        }
        authorization.refreshToken(refreshToken);
        return refreshToken;
    }

    private Set<String> authorizedScopes(
            ExternalIdentityGrantAuthenticationToken grant,
            RegisteredClient client
    ) {
        Set<String> configured;
        try {
            configured = clientScopeResolver.resolve(grant.request().clientAppId());
        } catch (ApplicationException exception) {
            throw oauth2(OAuth2ErrorCodes.INVALID_SCOPE);
        }
        if (!client.getScopes().containsAll(configured)) {
            throw oauth2(OAuth2ErrorCodes.SERVER_ERROR);
        }
        if (!StringUtils.hasText(grant.request().scope())) {
            return configured;
        }
        Set<String> requested;
        try {
            requested = Set.of(StringUtils.tokenizeToStringArray(grant.request().scope(), " "));
        } catch (IllegalArgumentException exception) {
            throw oauth2(OAuth2ErrorCodes.INVALID_SCOPE);
        }
        if (!configured.equals(requested)) {
            throw oauth2(OAuth2ErrorCodes.INVALID_SCOPE);
        }
        return configured;
    }

    private static OAuth2ClientAuthenticationToken authenticatedClient(
            ExternalIdentityGrantAuthenticationToken grant
    ) {
        if (grant.getPrincipal() instanceof OAuth2ClientAuthenticationToken client
                && client.isAuthenticated()) {
            return client;
        }
        throw oauth2(OAuth2ErrorCodes.INVALID_CLIENT);
    }

    private static Map<String, Object> responseParameters(
            ExternalAuthenticationCommandOutput login
    ) {
        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put(SasTokenResponseParameters.USER_ID, login.userId());
        parameters.put(SasTokenResponseParameters.AUTH_ACCOUNT_ID, login.authAccountId());
        parameters.put(SasTokenResponseParameters.SESSION_ID, login.sessionId());
        parameters.put(SasTokenResponseParameters.FROM_REGISTRATION_FLOW, false);
        parameters.put(SasTokenResponseParameters.REPLAYED, login.replayed());
        return parameters;
    }

    private static OAuth2AuthenticationException oauth2(String errorCode) {
        return new OAuth2AuthenticationException(new OAuth2Error(errorCode));
    }
}
