package com.cloud.userauth.application.authorization;

import java.time.Instant;
import java.util.List;

public record RoleView(
        String roleCode,
        String roleName,
        String status,
        List<String> permissionCodes,
        Instant createdAt,
        Instant updatedAt
) {
    public RoleView {
        permissionCodes = List.copyOf(permissionCodes);
    }
}
