package com.cloud.userauth.application.authorization;

import java.util.List;

public record UserAuthorizationView(
        Long userId,
        List<String> effectiveRoleCodes,
        List<String> effectivePermissionCodes,
        List<AuthorizationGrantView> roleGrants,
        List<AuthorizationGrantView> directPermissionGrants
) {
    public UserAuthorizationView {
        effectiveRoleCodes = List.copyOf(effectiveRoleCodes);
        effectivePermissionCodes = List.copyOf(effectivePermissionCodes);
        roleGrants = List.copyOf(roleGrants);
        directPermissionGrants = List.copyOf(directPermissionGrants);
    }

    public record AuthorizationGrantView(
            String targetCode,
            String sourceType,
            String sourceId,
            String status
    ) {
    }
}
