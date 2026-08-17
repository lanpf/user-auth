package com.cloud.userauth.api.authorization;

import com.cloud.framework.core.Request;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record SaveRoleApiCommand(
        @NotBlank String roleCode,
        @NotBlank String roleName,
        List<String> permissionCodes
) implements Request {
    public SaveRoleApiCommand {
        permissionCodes = permissionCodes == null ? List.of() : List.copyOf(permissionCodes);
    }
}
