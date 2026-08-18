package com.cloud.userauth.application.logout;

public record LogoutCommand(Long authenticatedUserId, String sessionId) {
}
