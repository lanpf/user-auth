package com.cloud.userauth.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(SensitiveDataProperties.PREFIX)
public class SensitiveDataProperties {
    public static final String PREFIX = "user-auth.sensitive-data";

    private String cipherKey;
}
