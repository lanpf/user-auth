package com.cloud.userauth.application.session.handoff;

public record CreateSessionHandoffCommand(
        Long authenticatedUserId,
        String authenticatedSessionId,
        SessionHandoffTarget target
) {
}
