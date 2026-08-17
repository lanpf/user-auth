package com.cloud.userauth.infrastructure.session.redis.config;

import jakarta.validation.constraints.AssertTrue;
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
@ConfigurationProperties("user-auth.authentication.h5-session")
public class H5SessionProperties {
    @NotNull
    @DurationMin(minutes = 1)
    private Duration idleTtl = Duration.ofMinutes(30);
    @NotNull
    @DurationMin(seconds = 30)
    private Duration absoluteTtl = Duration.ofHours(8);
    @NotNull
    @DurationMin(seconds = 30)
    private Duration renewalThreshold = Duration.ofMinutes(10);

    @AssertTrue(message = "H5 session renewal-threshold must not exceed idle-ttl")
    public boolean isRenewalThresholdWithinIdleTtl() {
        return renewalThreshold == null || idleTtl == null
                || renewalThreshold.compareTo(idleTtl) <= 0;
    }

    @AssertTrue(message = "H5 session idle-ttl must not exceed absolute-ttl")
    public boolean isIdleTtlWithinAbsoluteTtl() {
        return idleTtl == null || absoluteTtl == null
                || idleTtl.compareTo(absoluteTtl) <= 0;
    }

}
