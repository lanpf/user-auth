package com.cloud.userauth.api.authorization;

import com.cloud.framework.core.Request;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UserAuthorizationApiQuery(
        @NotNull @Positive Long userId
) implements Request {
}
