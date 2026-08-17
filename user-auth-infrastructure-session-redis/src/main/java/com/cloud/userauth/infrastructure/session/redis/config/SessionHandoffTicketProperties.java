package com.cloud.userauth.infrastructure.session.redis.config;

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
@ConfigurationProperties("user-auth.authentication.session-handoff")
public class SessionHandoffTicketProperties {
    @NotNull
    @DurationMin(seconds = 30)
    private Duration ttl = Duration.ofSeconds(60);
}
