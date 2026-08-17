package com.cloud.userauth.api.authorization;

import java.time.Instant;

public record PermissionApiResponse(
        String permissionCode,
        String permissionName,
        String ownerService,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
}
