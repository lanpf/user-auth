package com.cloud.userauth.api.authorization;

import com.cloud.framework.core.Request;
import jakarta.validation.constraints.NotBlank;

public record ChangePermissionStatusApiCommand(
        @NotBlank String permissionCode
) implements Request {
}
