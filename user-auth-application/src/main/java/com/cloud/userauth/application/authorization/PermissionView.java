package com.cloud.userauth.application.authorization;

import java.time.Instant;

public record PermissionView(
        String permissionCode,
        String permissionName,
        String ownerService,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
}
