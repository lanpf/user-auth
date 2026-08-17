package com.cloud.userauth.infrastructure.oauth2.redis.config;

import com.cloud.framework.core.naming.Namespaced;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "user-auth.authentication.oauth2.authorization-store")
public class OAuth2AuthorizationStoreProperties implements Namespaced {
    private String namespace;

    @NotBlank
    private String scene = "oauth2";
}
