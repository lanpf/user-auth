package com.cloud.userauth.infrastructure.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = NonProductionAuthChallengeProperties.PREFIX)
public class NonProductionAuthChallengeProperties {
    public static final String PREFIX = AuthChallengeProperties.PREFIX + ".non-production";
    @NotBlank
    @Pattern(
            regexp = "\\d{6}",
            message = PREFIX + ".fixed-code must contain exactly 6 digits"
    )
    private String fixedCode;

    private String sensitiveCipherKey;
}
