package com.cloud.userauth.infrastructure.challenge;

import com.cloud.userauth.application.port.OneTimeCodeGenerator;
import java.security.SecureRandom;

public class SecureOneTimeCodeGenerator implements OneTimeCodeGenerator {
    private final SecureRandom random = new SecureRandom();

    @Override
    public String generate() {
        return "%06d".formatted(random.nextInt(1_000_000));
    }
}
