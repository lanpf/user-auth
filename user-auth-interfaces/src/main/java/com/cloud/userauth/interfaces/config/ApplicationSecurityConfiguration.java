package com.cloud.userauth.interfaces.config;

import static org.springframework.security.oauth2.core.authorization.OAuth2AuthorizationManagers.hasScope;

import com.cloud.userauth.api.authentication.OAuth2Scope;
import com.cloud.userauth.application.port.H5SessionStore;
import com.cloud.userauth.interfaces.security.AuthenticatedSessionResolver;
import com.cloud.userauth.interfaces.security.H5SessionAuthenticationFilter;
import com.cloud.userauth.interfaces.security.SessionAuthenticationAuthorities;
import com.cloud.userauth.interfaces.rest.UserAuthRestPaths;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration(proxyBeanMethods = false)
public class ApplicationSecurityConfiguration {
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 100)
    public SecurityFilterChain applicationSecurityFilterChain(
            HttpSecurity http,
            ObjectProvider<H5SessionAuthenticationFilter> h5SessionAuthenticationFilter,
            ObjectProvider<JwtDecoder> jwtDecoder,
            ObjectProvider<OpaqueTokenIntrospector> opaqueTokenIntrospector
    ) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                UserAuthRestPaths.API_AUTH_CHALLENGES,
                                UserAuthRestPaths.API_LOGIN_MOBILE_OTP,
                                UserAuthRestPaths.API_LOGIN_REFRESH,
                                UserAuthRestPaths.API_LOGIN_EXTERNAL_ATTEMPTS,
                                UserAuthRestPaths.API_LOGIN_EXTERNAL,
                                UserAuthRestPaths.API_LOGIN_EXTERNAL_TRUSTED_MOBILE,
                                UserAuthRestPaths.API_LOGIN_EXTERNAL_BOUND_CREDENTIAL,
                                UserAuthRestPaths.API_WEB_VIEW_HANDOFFS_EXCHANGE,
                                "/actuator/health",
                                "/actuator/info")
                        .permitAll()
                        .requestMatchers(
                                UserAuthRestPaths.API_LOGOUT,
                                UserAuthRestPaths.API_CREDENTIALS_EXTERNAL_BIND,
                                UserAuthRestPaths.API_WEB_VIEW_HANDOFFS)
                        .hasAuthority(SessionAuthenticationAuthorities.HOST_SESSION)
                        .requestMatchers(UserAuthRestPaths.ADMIN_AUTHORIZATION + "/**")
                        .access(hasScope(OAuth2Scope.ADMIN.value()))
                        .anyRequest()
                        .denyAll())
                .csrf(csrf -> csrf.ignoringRequestMatchers(
                        UserAuthRestPaths.API_AUTH_CHALLENGES,
                        UserAuthRestPaths.API_LOGIN_MOBILE_OTP,
                        UserAuthRestPaths.API_LOGIN_REFRESH,
                        UserAuthRestPaths.API_LOGIN_EXTERNAL_ATTEMPTS,
                        UserAuthRestPaths.API_LOGIN_EXTERNAL,
                        UserAuthRestPaths.API_LOGIN_EXTERNAL_TRUSTED_MOBILE,
                        UserAuthRestPaths.API_LOGIN_EXTERNAL_BOUND_CREDENTIAL,
                        UserAuthRestPaths.API_WEB_VIEW_HANDOFFS,
                        UserAuthRestPaths.API_WEB_VIEW_HANDOFFS_EXCHANGE,
                        UserAuthRestPaths.API_LOGOUT,
                        UserAuthRestPaths.API_CREDENTIALS_EXTERNAL_BIND,
                        UserAuthRestPaths.ADMIN_AUTHORIZATION + "/**"));
        JwtDecoder decoder = jwtDecoder.getIfAvailable();
        if (decoder != null) {
            http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt
                    .decoder(decoder)
                    .jwtAuthenticationConverter(ApplicationSecurityConfiguration::authentication)));
        } else {
            OpaqueTokenIntrospector introspector = opaqueTokenIntrospector.getIfAvailable();
            if (introspector == null) {
                throw new IllegalStateException(
                        "OAuth2 resource server requires a JwtDecoder or OpaqueTokenIntrospector");
            }
            http.oauth2ResourceServer(oauth2 -> oauth2.opaqueToken(opaque -> opaque
                    .introspector(introspector)
                    .authenticationConverter(ApplicationSecurityConfiguration::authentication)));
        }
        H5SessionAuthenticationFilter h5Filter = h5SessionAuthenticationFilter.getIfAvailable();
        if (h5Filter != null) {
            http.addFilterBefore(h5Filter, BearerTokenAuthenticationFilter.class);
        }
        return http.build();
    }

    @Bean
    public H5SessionAuthenticationFilter h5SessionAuthenticationFilter(H5SessionStore sessionStore) {
        return new H5SessionAuthenticationFilter(sessionStore);
    }

    private static AbstractAuthenticationToken authentication(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>(
                SessionAuthenticationAuthorities.hostSession());
        authorities.addAll(new JwtGrantedAuthoritiesConverter().convert(jwt));
        return new UsernamePasswordAuthenticationToken(
                AuthenticatedSessionResolver.resolve(jwt),
                jwt,
                authorities);
    }

    private static AbstractAuthenticationToken authentication(
            String token,
            OAuth2AuthenticatedPrincipal principal
    ) {
        List<GrantedAuthority> authorities = new ArrayList<>(
                SessionAuthenticationAuthorities.hostSession());
        authorities.addAll(principal.getAuthorities());
        return new UsernamePasswordAuthenticationToken(
                AuthenticatedSessionResolver.resolve(principal),
                token,
                authorities);
    }
}
