package com.cloud.userauth.interfaces.config;

import com.cloud.userauth.api.constants.UserAuthPathApiConstants;
import com.cloud.userauth.interfaces.security.ForwardedAuthenticatedSessionFilter;
import com.cloud.userauth.interfaces.security.SessionAuthenticationAuthority;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration(proxyBeanMethods = false)
public class UserAuthApiSecurityConfiguration {
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 100)
    public SecurityFilterChain UserAuthApiSecurityFilterChain(
            HttpSecurity http,
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
                                UserAuthPathApiConstants.INTERNAL_BASE_PATH + "/**",
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
                        .requestMatchers(UserAuthPathApiConstants.API_BROWSER_SESSIONS + "/**")
                        .hasAuthority(SessionAuthenticationAuthority.BROWSER_SESSION.getValue())
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
                        UserAuthPathApiConstants.ADMIN_AUTHORIZATION + "/**",
                        UserAuthPathApiConstants.INTERNAL_BASE_PATH + "/**",
                        UserAuthPathApiConstants.API_BROWSER_SESSIONS_END));
        http.addFilterBefore(forwardedAuthenticatedSessionFilter, AnonymousAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public ForwardedAuthenticatedSessionFilter forwardedAuthenticatedSessionFilter() {
        return new ForwardedAuthenticatedSessionFilter();
    }
}
