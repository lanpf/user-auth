package com.cloud.userauth.application.session.handoff;

import java.time.Duration;

public record ExchangeH5SessionHandoffCommandOutput(
        String sessionCredential,
        Duration sessionTtl,
        String handoffId
) {
}
