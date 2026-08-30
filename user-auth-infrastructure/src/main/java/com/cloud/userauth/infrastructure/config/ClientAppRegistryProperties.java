package com.cloud.userauth.infrastructure.config;

import com.cloud.userauth.api.authentication.OAuth2Scope;
import com.cloud.userauth.application.port.ClientRenewalPolicy;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Validated
@ConfigurationProperties(ClientAppRegistryProperties.PREFIX)
public class ClientAppRegistryProperties {
    public static final String PREFIX = "user-auth.authentication";
    @NotEmpty
    private final Map<@NotBlank String, @NotNull @Valid ClientAppProperties> clientApps =
            new LinkedHashMap<>();

    @Getter
    @Setter
    public static class ClientAppProperties {
        @NotNull
        private ClientRenewalPolicy renewalPolicy;

        private final Set<@NotNull OAuth2Scope> oauth2Scopes =
                new LinkedHashSet<>();
    }
}
