package com.cloud.userauth.interfaces.config;

import com.cloud.userauth.application.port.BrowserSessionStore;
import com.cloud.userauth.interfaces.security.ForwardedAuthenticatedSessionFilter;
import com.cloud.userauth.interfaces.security.BrowserSessionAuthenticationFilter;
import com.cloud.userauth.interfaces.security.SessionAuthenticationAuthorities;
import com.cloud.userauth.interfaces.rest.UserAuthRestPaths;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration(proxyBeanMethods = false)
public class ApplicationSecurityConfiguration {
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 100)
    public SecurityFilterChain applicationSecurityFilterChain(
            HttpSecurity http,
            ObjectProvider<BrowserSessionAuthenticationFilter> browserSessionAuthenticationFilter,
            ForwardedAuthenticatedSessionFilter forwardedAuthenticatedSessionFilter
    ) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                UserAuthRestPaths.API_CHALLENGES,
                                UserAuthRestPaths.API_LOGIN_MOBILE_OTP,
                                UserAuthRestPaths.API_LOGIN_REFRESH,
                                UserAuthRestPaths.API_LOGIN_PARTNER_TRUSTED_MOBILE,
                                UserAuthRestPaths.API_LOGIN_EXTERNAL_PROOF,
                                UserAuthRestPaths.API_LOGIN_EXTERNAL_BOUND_CREDENTIAL,
                                UserAuthRestPaths.API_LOGIN_EXTERNAL_ATTEMPT,
                                UserAuthRestPaths.API_LOGIN_EXTERNAL_COMPLETE,
                                UserAuthRestPaths.API_HANDOFFS_EXCHANGE,
                                "/actuator/health",
                                "/actuator/info")
                        .permitAll()
                        .requestMatchers(
                                UserAuthRestPaths.API_LOGOUT,
                                UserAuthRestPaths.API_CREDENTIALS_BIND,
                                UserAuthRestPaths.API_HANDOFFS)
                        .hasAuthority(SessionAuthenticationAuthorities.HOST_SESSION)
                        .requestMatchers(UserAuthRestPaths.ADMIN_AUTHORIZATION + "/**")
                        .hasAuthority(SessionAuthenticationAuthorities.HOST_SESSION)
                        .anyRequest()
                        .denyAll())
                .csrf(csrf -> csrf.ignoringRequestMatchers(
                        UserAuthRestPaths.API_CHALLENGES,
                        UserAuthRestPaths.API_LOGIN_MOBILE_OTP,
                        UserAuthRestPaths.API_LOGIN_REFRESH,
                        UserAuthRestPaths.API_LOGIN_PARTNER_TRUSTED_MOBILE,
                        UserAuthRestPaths.API_LOGIN_EXTERNAL_PROOF,
                        UserAuthRestPaths.API_LOGIN_EXTERNAL_BOUND_CREDENTIAL,
                        UserAuthRestPaths.API_LOGIN_EXTERNAL_ATTEMPT,
                        UserAuthRestPaths.API_LOGIN_EXTERNAL_COMPLETE,
                        UserAuthRestPaths.API_HANDOFFS,
                        UserAuthRestPaths.API_HANDOFFS_EXCHANGE,
                        UserAuthRestPaths.API_LOGOUT,
                        UserAuthRestPaths.API_CREDENTIALS_BIND,
                        UserAuthRestPaths.ADMIN_AUTHORIZATION + "/**"));
        http.addFilterBefore(forwardedAuthenticatedSessionFilter, AnonymousAuthenticationFilter.class);
        browserSessionAuthenticationFilter.ifAvailable(filter -> http.addFilterAfter(filter, ForwardedAuthenticatedSessionFilter.class));

        return http.build();
    }

    @Bean
    public BrowserSessionAuthenticationFilter browserSessionAuthenticationFilter(BrowserSessionStore browserSessionStore) {
        return new BrowserSessionAuthenticationFilter(browserSessionStore);
    }

    @Bean
    public ForwardedAuthenticatedSessionFilter forwardedAuthenticatedSessionFilter() {
        return new ForwardedAuthenticatedSessionFilter();
    }
}
