package com.cloud.userauth.application.authorization;

public record SavePermissionCommand(
        String permissionCode,
        String permissionName,
        String ownerService
) {
}
