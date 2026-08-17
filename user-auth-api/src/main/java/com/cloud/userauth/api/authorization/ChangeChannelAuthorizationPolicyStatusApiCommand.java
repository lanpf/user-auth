package com.cloud.userauth.api.authorization;

import com.cloud.framework.core.Request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChangeChannelAuthorizationPolicyStatusApiCommand(
        @NotBlank String channelCode,
        @NotNull Long expectedVersion
) implements Request {
}
