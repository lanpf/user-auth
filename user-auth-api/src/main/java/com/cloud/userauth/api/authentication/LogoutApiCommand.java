package com.cloud.userauth.api.authentication;

import com.cloud.framework.core.Request;
import jakarta.validation.constraints.NotBlank;

public record LogoutApiCommand(@NotBlank String sessionId) implements Request {
}
