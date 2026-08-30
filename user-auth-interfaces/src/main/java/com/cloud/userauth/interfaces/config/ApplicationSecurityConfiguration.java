package com.cloud.userauth.interfaces.config;

import com.cloud.userauth.application.port.BrowserSessionStore;
import com.cloud.userauth.interfaces.security.ForwardedAuthenticatedSessionFilter;
import com.cloud.userauth.interfaces.security.BrowserSessionAuthenticationFilter;
import com.cloud.userauth.interfaces.security.SessionAuthenticationAuthority;
import com.cloud.userauth.api.constants.UserAuthPathApiConstants;
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
                                UserAuthPathApiConstants.API_CHALLENGES,
                                UserAuthPathApiConstants.API_LOGIN_MOBILE_OTP,
                                UserAuthPathApiConstants.API_LOGIN_REFRESH,
                                UserAuthPathApiConstants.API_LOGIN_PARTNER_TRUSTED_MOBILE,
                                UserAuthPathApiConstants.API_LOGIN_EXTERNAL_PROOF,
                                UserAuthPathApiConstants.API_LOGIN_EXTERNAL_BOUND_CREDENTIAL,
                                UserAuthPathApiConstants.API_LOGIN_EXTERNAL_ATTEMPT,
                                UserAuthPathApiConstants.API_LOGIN_EXTERNAL_COMPLETE,
                                UserAuthPathApiConstants.API_HANDOFFS_EXCHANGE,
                                "/actuator/health",
                                "/actuator/info")
                        .permitAll()
                        .requestMatchers(
                                UserAuthPathApiConstants.API_LOGOUT,
                                UserAuthPathApiConstants.API_CREDENTIALS_BIND,
                                UserAuthPathApiConstants.API_HANDOFFS)
                        .hasAuthority(SessionAuthenticationAuthority.HOST_SESSION.getValue())
                        .requestMatchers(UserAuthPathApiConstants.ADMIN_AUTHORIZATION + "/**")
                        .hasAuthority(SessionAuthenticationAuthority.HOST_SESSION.getValue())
                        .anyRequest()
                        .denyAll())
                .csrf(csrf -> csrf.ignoringRequestMatchers(
                        UserAuthPathApiConstants.API_CHALLENGES,
                        UserAuthPathApiConstants.API_LOGIN_MOBILE_OTP,
                        UserAuthPathApiConstants.API_LOGIN_REFRESH,
                        UserAuthPathApiConstants.API_LOGIN_PARTNER_TRUSTED_MOBILE,
                        UserAuthPathApiConstants.API_LOGIN_EXTERNAL_PROOF,
                        UserAuthPathApiConstants.API_LOGIN_EXTERNAL_BOUND_CREDENTIAL,
                        UserAuthPathApiConstants.API_LOGIN_EXTERNAL_ATTEMPT,
                        UserAuthPathApiConstants.API_LOGIN_EXTERNAL_COMPLETE,
                        UserAuthPathApiConstants.API_HANDOFFS,
                        UserAuthPathApiConstants.API_HANDOFFS_EXCHANGE,
                        UserAuthPathApiConstants.API_LOGOUT,
                        UserAuthPathApiConstants.API_CREDENTIALS_BIND,
                        UserAuthPathApiConstants.ADMIN_AUTHORIZATION + "/**"));
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
