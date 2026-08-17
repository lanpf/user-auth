package com.cloud.userauth.infrastructure.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
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
@ConfigurationProperties(prefix = "user-auth.authentication.challenge")
public class AuthChallengeProperties {
    @NotBlank
    private String pepper;

    @Valid
    private final IssuePolicyProperties issuePolicy = new IssuePolicyProperties();

    @Getter
    @Setter
    public static class IssuePolicyProperties {
        @NotNull
        @DurationMin(seconds = 1,
                message = "user-auth.authentication.challenge.issue-policy.ttl must be at least 1 second")
        private Duration ttl = Duration.ofMinutes(5);

        @NotNull
        @DurationMin(
                seconds = 1,
                message = "user-auth.authentication.challenge.issue-policy.reuse-window must be at least 1 second")
        private Duration reuseWindow = Duration.ofSeconds(60);

        @AssertTrue(message = "user-auth.authentication.challenge.issue-policy.reuse-window must not exceed ttl")
        public boolean isReuseWindowWithinTtl() {
            return reuseWindow == null || ttl == null || reuseWindow.compareTo(ttl) <= 0;
        }
    }
}
