package com.cloud.userauth.application.logout;

import com.cloud.userauth.domain.authentication.session.SessionStatus;

public record LogoutOutput(
        String sessionId,
        SessionStatus sessionStatus
) {
}
