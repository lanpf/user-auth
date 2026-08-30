package com.cloud.userauth.infrastructure.config;

import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = LoginSessionProperties.PREFIX)
public class LoginSessionProperties {
    public static final String PREFIX = "user-auth.authentication.login-session";

    @NotNull
    @DurationMin(days = 1, message = PREFIX + ".ttl must be at least 1 day")
    private Duration ttl = Duration.ofDays(30);
}
