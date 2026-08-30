package com.cloud.userauth.infrastructure.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = ExternalIdentityProperties.PREFIX)
public class ExternalIdentityProperties {
    public static final String PREFIX = "user-auth.authentication.external-identity";
    @Valid
    private final LoginAttemptProperties loginAttempt = new LoginAttemptProperties();

    private final Map<@NotBlank String, @NotNull @Valid IssuerPolicyProperties> issuerPolicies =
            new LinkedHashMap<>();

    @Getter
    @Setter
    public static class LoginAttemptProperties {
        @NotNull
        @DurationMin(
                seconds = 1,
                message = PREFIX + ".login-attempt.ttl must be at least 1 second")
        private Duration ttl = Duration.ofMinutes(10);
    }

    @Getter
    @Setter
    public static class IssuerPolicyProperties {
        @NotNull
        private Boolean trustVerifiedMobile;
    }
}
