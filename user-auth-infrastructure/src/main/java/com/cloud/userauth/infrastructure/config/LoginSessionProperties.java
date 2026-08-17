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
@ConfigurationProperties(prefix = "user-auth.authentication.login-session")
public class LoginSessionProperties {
    @NotNull
    @DurationMin(days = 1, message = "user-auth.authentication.login-session.ttl must be at least 1 day")
    private Duration ttl = Duration.ofDays(30);
}
