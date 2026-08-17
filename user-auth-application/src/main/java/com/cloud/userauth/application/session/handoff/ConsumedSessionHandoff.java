package com.cloud.userauth.application.session.handoff;

import com.cloud.userauth.application.authentication.AuthenticatedSession;

public record ConsumedSessionHandoff(
        AuthenticatedSession authenticatedSession,
        String handoffId
) {
}
