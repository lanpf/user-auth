package com.cloud.userauth.infrastructure.oauth2.sas.config;

import com.cloud.framework.core.http.RestClientProperties;
import com.cloud.userauth.application.login.MobileOtpAuthenticationProcess;
import com.cloud.userauth.application.login.external.ExternalAuthenticationProcess;
import com.cloud.userauth.application.port.LoginTokenIssuer;
import com.cloud.userauth.application.port.LoginTokenRefresher;
import com.cloud.userauth.application.port.RefreshTokenRotationLock;
import com.cloud.userauth.application.port.SessionAuthorizationRevoker;
import com.cloud.userauth.application.port.ClientRenewalPolicyResolver;
import com.cloud.userauth.infrastructure.oauth2.sas.logout.SasSessionAuthorizationRevoker;
import com.cloud.userauth.infrastructure.oauth2.sas.grant.mobileotp.MobileOtpGrantAuthenticationConverter;
import com.cloud.userauth.infrastructure.oauth2.sas.grant.mobileotp.MobileOtpGrantAuthenticationProvider;
import com.cloud.userauth.infrastructure.oauth2.sas.grant.mobileotp.MobileOtpGrantRequestParser;
import com.cloud.userauth.infrastructure.oauth2.sas.grant.mobileotp.mapper.MobileOtpGrantRequestMapper;
import com.cloud.userauth.infrastructure.oauth2.sas.grant.external.ExternalIdentityGrantAuthenticationConverter;
import com.cloud.userauth.infrastructure.oauth2.sas.grant.external.ExternalIdentityGrantAuthenticationProvider;
import com.cloud.userauth.infrastructure.oauth2.sas.grant.external.ExternalIdentityGrantRequestParser;
import com.cloud.userauth.infrastructure.oauth2.sas.grant.external.mapper.ExternalIdentityGrantRequestMapper;
import com.cloud.userauth.infrastructure.oauth2.sas.login.SasLoginTokenIssuer;
import com.cloud.userauth.infrastructure.oauth2.sas.login.SasLoginTokenRefresher;
import com.cloud.userauth.infrastructure.oauth2.sas.login.mapper.SasExternalTokenEndpointPayloadMapper;
import com.cloud.userauth.infrastructure.oauth2.sas.login.mapper.SasTokenEndpointPayloadMapper;
import com.cloud.userauth.infrastructure.oauth2.sas.login.tokenendpoint.RestClientSasTokenEndpointClient;
import com.cloud.userauth.infrastructure.oauth2.sas.login.tokenendpoint.SasTokenEndpointClient;
import com.cloud.userauth.infrastructure.oauth2.sas.login.tokenendpoint.SasTokenEndpointResponseErrorHandler;
import com.cloud.userauth.infrastructure.oauth2.sas.login.tokenendpoint.SasTokenEndpointJsonMapper;
import com.cloud.userauth.infrastructure.oauth2.sas.scope.PropertiesSasClientScopeResolver;
import com.cloud.userauth.infrastructure.oauth2.sas.scope.SasClientScopeResolver;
import com.cloud.userauth.infrastructure.oauth2.sas.config.validation.SasClientScopeConfigurationValidator;
import com.cloud.userauth.infrastructure.config.ClientAppRegistryProperties;
import com.cloud.userauth.domain.authentication.session.LoginSessionRepository;
import java.time.Clock;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.net.URI;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(
        prefix = "user-auth.authentication.oauth2.authorization-server",
        name = "enabled",
        havingValue = "true")
@EnableConfigurationProperties(SasAuthorizationServerProperties.class)
public class SasAuthorizationServerConfiguration {
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authorizationServerSecurityFilterChain(
            HttpSecurity http,
            MobileOtpGrantAuthenticationConverter authenticationConverter,
            MobileOtpGrantAuthenticationProvider authenticationProvider,
            ExternalIdentityGrantAuthenticationConverter externalAuthenticationConverter,
            ExternalIdentityGrantAuthenticationProvider externalAuthenticationProvider,
            SasAuthorizationServerProperties properties
    ) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServer = OAuth2AuthorizationServerConfigurer.authorizationServer();
        if (properties.getOidc().isEnabled()) {
            authorizationServer.oidc(Customizer.withDefaults());
        }
        RequestMatcher endpointsMatcher = authorizationServer.getEndpointsMatcher();
        http.securityMatcher(endpointsMatcher)
                .with(authorizationServer, server -> server.tokenEndpoint(tokenEndpoint -> tokenEndpoint
                        .accessTokenRequestConverter(authenticationConverter)
                        .accessTokenRequestConverter(externalAuthenticationConverter)
                        .authenticationProvider(authenticationProvider)
                        .authenticationProvider(externalAuthenticationProvider)))
                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                .csrf(csrf -> csrf.ignoringRequestMatchers(endpointsMatcher));
        return http.build();
    }

    @Bean
    public PropertiesSasClientScopeResolver sasClientScopeResolver(
            ClientAppRegistryProperties properties
    ) {
        return new PropertiesSasClientScopeResolver(properties);
    }

    @Bean
    public SasClientScopeConfigurationValidator sasClientScopeConfigurationValidator(
            SasAuthorizationServerProperties sasProperties,
            ClientAppRegistryProperties clientAppProperties
    ) {
        return new SasClientScopeConfigurationValidator(sasProperties, clientAppProperties);
    }

    @Bean
    public MobileOtpGrantRequestParser mobileOtpGrantRequestParser(
            Validator validator
    ) {
        return new MobileOtpGrantRequestParser(validator);
    }

    @Bean
    public MobileOtpGrantAuthenticationConverter mobileOtpGrantAuthenticationConverter(
            MobileOtpGrantRequestParser requestParser
    ) {
        return new MobileOtpGrantAuthenticationConverter(requestParser);
    }

    @Bean
    public ExternalIdentityGrantRequestParser externalIdentityGrantRequestParser(
            Validator validator
    ) {
        return new ExternalIdentityGrantRequestParser(validator);
    }

    @Bean
    public ExternalIdentityGrantAuthenticationConverter externalIdentityGrantAuthenticationConverter(
            ExternalIdentityGrantRequestParser requestParser
    ) {
        return new ExternalIdentityGrantAuthenticationConverter(requestParser);
    }

    @Bean
    public SasTokenEndpointJsonMapper sasTokenEndpointJsonMapper(
            ObjectMapper objectMapper
    ) {
        return new SasTokenEndpointJsonMapper(objectMapper);
    }

    @Bean
    public SasTokenEndpointResponseErrorHandler sasTokenEndpointErrorHandler(
            SasTokenEndpointJsonMapper jsonMapper
    ) {
        return new SasTokenEndpointResponseErrorHandler(jsonMapper);
    }

    @Bean
    public SasTokenEndpointClient sasTokenEndpointClient(
            SasAuthorizationServerProperties properties,
            SasTokenEndpointResponseErrorHandler errorHandler,
            SasTokenEndpointJsonMapper jsonMapper,
            Validator validator,
            @Value("${server.port:8081}") int serverPort
    ) {
        SimpleClientHttpRequestFactory requestFactory =
                new SimpleClientHttpRequestFactory();
        RestClientProperties restClientProperties =
                properties.getInternalTokenClient().getRestClient();
        requestFactory.setConnectTimeout(
                Math.toIntExact(restClientProperties
                        .getConnectTimeout().toMillis()));
        requestFactory.setReadTimeout(
                Math.toIntExact(restClientProperties
                        .getReadTimeout().toMillis()));
        RestClient restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .messageConverters(converters -> {
                    converters.removeIf(
                            MappingJackson2HttpMessageConverter.class::isInstance);
                    converters.add(jsonMapper.messageConverter());
                })
                .defaultStatusHandler(errorHandler)
                .build();
        return new RestClientSasTokenEndpointClient(
                restClient,
                internalTokenEndpoint(properties, serverPort),
                properties.getInternalTokenClient().getClientId(),
                properties.getInternalTokenClient().getClientSecret(),
                validator);
    }

    @Bean
    @ConditionalOnMissingBean(LoginTokenIssuer.class)
    public LoginTokenIssuer loginTokenIssuer(
            SasClientScopeResolver clientScopeResolver,
            SasTokenEndpointClient tokenEndpointClient,
            MobileOtpGrantRequestMapper requestMapper,
            SasTokenEndpointPayloadMapper responseMapper,
            ExternalIdentityGrantRequestMapper externalRequestMapper,
            SasExternalTokenEndpointPayloadMapper externalResponseMapper
    ) {
        return new SasLoginTokenIssuer(
                clientScopeResolver,
                tokenEndpointClient,
                requestMapper,
                responseMapper,
                externalRequestMapper,
                externalResponseMapper);
    }

    @Bean
    @ConditionalOnMissingBean(LoginTokenRefresher.class)
    public LoginTokenRefresher loginTokenRefresher(
            OAuth2AuthorizationService authorizationService,
            SasTokenEndpointClient tokenEndpointClient,
            LoginSessionRepository loginSessionRepository,
            Clock clock,
            RefreshTokenRotationLock rotationLock
    ) {
        return new SasLoginTokenRefresher(
                authorizationService, tokenEndpointClient, loginSessionRepository, clock,
                rotationLock);
    }

    @Bean
    @ConditionalOnMissingBean(SessionAuthorizationRevoker.class)
    public SessionAuthorizationRevoker sessionAuthorizationRevoker(
            OAuth2AuthorizationService authorizationService
    ) {
        return new SasSessionAuthorizationRevoker(authorizationService);
    }

    @Bean
    public MobileOtpGrantAuthenticationProvider mobileOtpGrantAuthenticationProvider(
            MobileOtpAuthenticationProcess mobileOtpAuthenticationProcess,
            OAuth2AuthorizationService authorizationService,
            OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator,
            PropertiesSasClientScopeResolver clientScopeResolver,
            MobileOtpGrantRequestMapper requestMapper,
            ClientRenewalPolicyResolver renewalPolicyResolver
    ) {
        return new MobileOtpGrantAuthenticationProvider(
                mobileOtpAuthenticationProcess,
                authorizationService,
                tokenGenerator,
                clientScopeResolver,
                requestMapper,
                renewalPolicyResolver);
    }

    @Bean
    public ExternalIdentityGrantAuthenticationProvider externalIdentityGrantAuthenticationProvider(
            ExternalAuthenticationProcess externalAuthenticationProcess,
            OAuth2AuthorizationService authorizationService,
            OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator,
            PropertiesSasClientScopeResolver clientScopeResolver,
            ExternalIdentityGrantRequestMapper requestMapper,
            ClientRenewalPolicyResolver renewalPolicyResolver
    ) {
        return new ExternalIdentityGrantAuthenticationProvider(
                externalAuthenticationProcess,
                authorizationService,
                tokenGenerator,
                clientScopeResolver,
                requestMapper,
                renewalPolicyResolver);
    }

    private static URI internalTokenEndpoint(
            SasAuthorizationServerProperties properties,
            int serverPort
    ) {
        String configured = properties.getInternalTokenClient().getTokenEndpoint();
        return URI.create(StringUtils.hasText(configured)
                ? configured
                : "http://127.0.0.1:" + serverPort + "/oauth2/token");
    }
}
