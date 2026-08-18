package com.cloud.userauth.application.session.handoff;

import java.time.Duration;

public record ExchangeSessionHandoffOutput(
        String sessionCredential,
        Duration sessionTtl,
        String handoffId
) {
}
