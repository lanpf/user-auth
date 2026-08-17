package com.cloud.userauth.api.authorization;

import com.cloud.framework.core.Request;
import jakarta.validation.constraints.NotBlank;

public record ChannelAuthorizationPolicyApiQuery(
        @NotBlank String channelCode
) implements Request {
}
