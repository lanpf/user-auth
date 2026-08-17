package com.cloud.userauth;

import com.cloud.userauth.config.AuthenticationRuntimeProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(
        scanBasePackages = "com.cloud.userauth",
        excludeName = {
                "org.springframework.boot.autoconfigure.security.oauth2.server.servlet."
                        + "OAuth2AuthorizationServerAutoConfiguration",
                "org.springframework.boot.autoconfigure.security.oauth2.server.servlet."
                        + "OAuth2AuthorizationServerJwtAutoConfiguration"
        })
@EnableConfigurationProperties(AuthenticationRuntimeProperties.class)
public class UserAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserAuthApplication.class, args);
    }
}
