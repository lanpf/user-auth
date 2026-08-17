package com.cloud.userauth.application.logout;

import com.cloud.userauth.domain.authentication.session.SessionStatus;

public record LogoutCommandOutput(
        String sessionId,
        SessionStatus sessionStatus
) {
}
