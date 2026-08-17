package com.cloud.userauth.application.authorization;

import java.util.List;

public record SaveRoleCommand(
        String roleCode,
        String roleName,
        List<String> permissionCodes
) {
    public SaveRoleCommand {
        permissionCodes = permissionCodes == null ? List.of() : List.copyOf(permissionCodes);
    }
}
