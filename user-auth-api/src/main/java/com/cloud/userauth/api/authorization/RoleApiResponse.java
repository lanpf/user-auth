package com.cloud.userauth.api.authorization;

import java.time.Instant;
import java.util.List;

public record RoleApiResponse(
        String roleCode,
        String roleName,
        String status,
        List<String> permissionCodes,
        Instant createdAt,
        Instant updatedAt
) {
    public RoleApiResponse {
        permissionCodes = List.copyOf(permissionCodes);
    }
}
