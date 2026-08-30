package com.cloud.userauth.infrastructure.session.redis.config;

import com.cloud.framework.core.naming.Namespaced;
import jakarta.validation.constraints.AssertTrue;
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
@ConfigurationProperties(BrowserSessionProperties.PREFIX)
public class BrowserSessionProperties implements Namespaced {
    public static final String PREFIX = "user-auth.authentication.browser-session";
    private String namespace;

    @NotNull
    @DurationMin(minutes = 1)
    private Duration idleTtl = Duration.ofMinutes(30);

    @NotNull
    @DurationMin(seconds = 30)
    private Duration absoluteTtl = Duration.ofHours(8);

    @NotNull
    @DurationMin(seconds = 30)
    private Duration renewalThreshold = Duration.ofMinutes(10);

    @AssertTrue(message = PREFIX + ".renewal-threshold must not exceed idle-ttl")
    public boolean isRenewalThresholdWithinIdleTtl() {
        return renewalThreshold == null || idleTtl == null
                || renewalThreshold.compareTo(idleTtl) <= 0;
    }

    @AssertTrue(message = PREFIX + ".idle-ttl must not exceed absolute-ttl")
    public boolean isIdleTtlWithinAbsoluteTtl() {
        return idleTtl == null || absoluteTtl == null
                || idleTtl.compareTo(absoluteTtl) <= 0;
    }
}
