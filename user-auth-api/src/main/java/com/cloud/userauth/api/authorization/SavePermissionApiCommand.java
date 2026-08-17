package com.cloud.userauth.api.authorization;

import com.cloud.framework.core.Request;
import jakarta.validation.constraints.NotBlank;

public record SavePermissionApiCommand(
        @NotBlank String permissionCode,
        @NotBlank String permissionName,
        @NotBlank String ownerService
) implements Request {
}
