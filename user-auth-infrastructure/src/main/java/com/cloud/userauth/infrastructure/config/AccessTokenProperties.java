package com.cloud.userauth.infrastructure.config;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Getter
@Setter
@Validated
@ConfigurationProperties("user-auth.authentication.access-token")
public class AccessTokenProperties {

    @NotNull
    private Provider provider;

    @NotNull
    @DurationMin(seconds = 60)
    private Duration ttl = Duration.ofMinutes(15);

    public enum Provider {
        SAS,
        SESSION_TOKEN
    }
}
