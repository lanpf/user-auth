package com.cloud.userauth.infrastructure.challenge;

import com.cloud.userauth.application.port.OneTimeCodeGenerator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FixedCodeGenerator implements OneTimeCodeGenerator {
    private final String fixedCode;

    @Override
    public String generate() {
        return fixedCode;
    }
}
