package com.cloud.userauth.infrastructure.config;

import com.cloud.userauth.application.port.ClientRenewalPolicy;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Validated
@ConfigurationProperties("user-auth.authentication")
public class ClientAppRegistryProperties {
    @NotEmpty
    private final Map<@NotBlank String, @NotNull @Valid ClientAppProperties> clientApps =
            new LinkedHashMap<>();

    @Getter
    @Setter
    public static class ClientAppProperties {
        private static final String OAUTH2_SCOPE_PATTERN = "^(?!SCOPE_)[^\\s]+$";

        @NotNull
        private ClientRenewalPolicy renewalPolicy;

        private final Set<
                @NotBlank
                @Pattern(regexp = OAUTH2_SCOPE_PATTERN)
                String> oauth2Scopes = new LinkedHashSet<>();
    }
}
