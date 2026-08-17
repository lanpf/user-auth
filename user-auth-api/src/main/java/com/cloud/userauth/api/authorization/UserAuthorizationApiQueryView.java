package com.cloud.userauth.api.authorization;

import java.util.List;

public record UserAuthorizationApiQueryView(
        Long userId,
        List<String> effectiveRoleCodes,
        List<String> effectivePermissionCodes,
        List<AuthorizationGrantApiQueryView> roleGrants,
        List<AuthorizationGrantApiQueryView> directPermissionGrants
) {
    public UserAuthorizationApiQueryView {
        effectiveRoleCodes = List.copyOf(effectiveRoleCodes);
        effectivePermissionCodes = List.copyOf(effectivePermissionCodes);
        roleGrants = List.copyOf(roleGrants);
        directPermissionGrants = List.copyOf(directPermissionGrants);
    }

    public record AuthorizationGrantApiQueryView(
            String targetCode,
            String sourceType,
            String sourceId,
            String status
    ) {
    }
}
