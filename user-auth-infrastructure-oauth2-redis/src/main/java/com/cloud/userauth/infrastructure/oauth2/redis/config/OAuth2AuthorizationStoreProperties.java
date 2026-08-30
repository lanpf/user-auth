package com.cloud.userauth.infrastructure.oauth2.redis.config;

import com.cloud.framework.core.naming.Namespaced;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = OAuth2AuthorizationStoreProperties.PREFIX)
public class OAuth2AuthorizationStoreProperties implements Namespaced {
    public static final String PREFIX = "user-auth.authentication.oauth2.authorization-store";
    private String namespace;
}
