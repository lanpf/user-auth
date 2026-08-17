package com.cloud.userauth.api.authorization;

import com.cloud.framework.core.Request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ReconcileChannelAuthorizationPolicyApiCommand(
        @NotBlank String channelCode,
        @Min(1) @Max(200) int batchSize
) implements Request {
}
