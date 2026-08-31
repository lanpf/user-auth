package com.cloud.userauth.infrastructure.challenge;

import com.cloud.userauth.application.port.OneTimeCodeGenerator;
import com.cloud.userauth.infrastructure.config.NonProductionAuthChallengeProperties;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FixedOneTimeCodeGenerator implements OneTimeCodeGenerator {
    private final String fixed;

    @Override
    public String generate() {
        return properties.getFixedCode();
    }
}
