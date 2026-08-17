package com.cloud.userauth.api.authorization;

import com.cloud.framework.core.Request;
import jakarta.validation.constraints.NotBlank;

public record RoleApiQuery(@NotBlank String roleCode) implements Request {
}
