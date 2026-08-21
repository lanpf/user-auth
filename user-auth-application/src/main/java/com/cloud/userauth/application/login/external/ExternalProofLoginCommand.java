package com.cloud.userauth.application.login.external;

import com.cloud.userauth.domain.authentication.external.ProofType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record ExternalProofLoginCommand(
        @NotBlank String issuer,
        @NotNull ProofType proofType,
        @NotEmpty Map<@NotBlank String, @NotBlank String> proofParameters,
        String deviceId,
        String deviceType,
        String deviceName,
        @NotBlank String clientAppId,
        String clientPlatform,
        String clientVersion,
        @NotBlank String channelCode
) {
    public ExternalProofLoginCommand {
        proofParameters = proofParameters == null ? null : Map.copyOf(proofParameters);
    }
}
