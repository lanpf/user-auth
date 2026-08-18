package com.cloud.userauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
        scanBasePackages = "com.cloud.userauth",
        excludeName = {
                "org.springframework.boot.autoconfigure.security.oauth2.server.servlet."
                        + "OAuth2AuthorizationServerAutoConfiguration",
                "org.springframework.boot.autoconfigure.security.oauth2.server.servlet."
                        + "OAuth2AuthorizationServerJwtAutoConfiguration"
        })
public class UserAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserAuthApplication.class, args);
    }
}
