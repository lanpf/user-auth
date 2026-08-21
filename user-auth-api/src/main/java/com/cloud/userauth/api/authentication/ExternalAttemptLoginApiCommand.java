package com.cloud.userauth.api.authentication;

import com.cloud.framework.core.Request;
import com.cloud.userauth.api.enums.ExternalProofTypeApiEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record ExternalAttemptLoginApiCommand(
        @NotBlank String issuer,
        @NotNull ExternalProofTypeApiEnum proofType,
        @NotEmpty Map<@NotBlank String, @NotBlank String> proofParameters,
        @NotBlank String clientAppId,
        String clientPlatform,
        String clientVersion,
        @NotBlank String channelCode
) implements Request {
    public ExternalAttemptLoginApiCommand {
        proofParameters = proofParameters == null ? null : Map.copyOf(proofParameters);
    }
}
