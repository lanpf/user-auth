package com.cloud.userauth.config;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Validated
@ConfigurationProperties("user-auth.authentication")
public class AuthenticationRuntimeProperties {
    @Valid
    private final OAuth2Properties oauth2 = new OAuth2Properties();

    @Getter
    @Setter
    public static class OAuth2Properties {
        @Valid
        private final AuthorizationServerProperties authorizationServer =
                new AuthorizationServerProperties();
    }

    @Getter
    @Setter
    public static class AuthorizationServerProperties {
        private boolean enabled;

    }
}
