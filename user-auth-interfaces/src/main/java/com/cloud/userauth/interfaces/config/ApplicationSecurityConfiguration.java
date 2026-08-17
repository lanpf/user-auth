package com.cloud.userauth.interfaces.config;

import com.cloud.userauth.application.port.SessionTokenStore;
import com.cloud.userauth.application.port.H5SessionStore;
import com.cloud.userauth.interfaces.security.AuthenticatedSessionResolver;
import com.cloud.userauth.interfaces.security.SessionTokenAuthenticationFilter;
import com.cloud.userauth.interfaces.security.H5SessionAuthenticationFilter;
import com.cloud.userauth.interfaces.security.SessionAuthenticationAuthorities;
import com.cloud.userauth.interfaces.rest.UserAuthRestPaths;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import java.util.ArrayList;
import org.springframework.security.core.GrantedAuthority;

@Configuration(proxyBeanMethods = false)
public class ApplicationSecurityConfiguration {
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 100)
    public SecurityFilterChain applicationSecurityFilterChain(
            HttpSecurity http,
            ObjectProvider<SessionTokenAuthenticationFilter> sessionTokenAuthenticationFilter,
            ObjectProvider<H5SessionAuthenticationFilter> h5SessionAuthenticationFilter,
            ObjectProvider<JwtDecoder> jwtDecoder
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
                        .hasAuthority("SCOPE_admin.api")
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
        }
        H5SessionAuthenticationFilter h5Filter = h5SessionAuthenticationFilter.getIfAvailable();
        if (h5Filter != null) {
            http.addFilterBefore(h5Filter, BearerTokenAuthenticationFilter.class);
        }
        SessionTokenAuthenticationFilter sessionTokenFilter = sessionTokenAuthenticationFilter.getIfAvailable();
        if (sessionTokenFilter != null) {
            if (h5Filter == null) {
                http.addFilterBefore(sessionTokenFilter, BearerTokenAuthenticationFilter.class);
            } else {
                http.addFilterBefore(sessionTokenFilter, H5SessionAuthenticationFilter.class);
            }
        }
        return http.build();
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "user-auth.authentication.access-token",
            name = "provider",
            havingValue = "session-token")
    public SessionTokenAuthenticationFilter sessionTokenAuthenticationFilter(
            SessionTokenStore sessionTokenStore
    ) {
        return new SessionTokenAuthenticationFilter(sessionTokenStore);
    }

    @Bean
    public H5SessionAuthenticationFilter h5SessionAuthenticationFilter(H5SessionStore sessionStore) {
        return new H5SessionAuthenticationFilter(sessionStore);
    }

    private static AbstractAuthenticationToken authentication(Jwt jwt) {
        ArrayList<GrantedAuthority> authorities = new ArrayList<>(
                SessionAuthenticationAuthorities.hostSession());
        authorities.addAll(new JwtGrantedAuthoritiesConverter().convert(jwt));
        return new UsernamePasswordAuthenticationToken(
                AuthenticatedSessionResolver.resolve(jwt),
                jwt,
                authorities);
    }
}
