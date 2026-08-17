package com.cloud.userauth.application.login.external;

import com.cloud.userauth.domain.authentication.external.ProofType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record ExternalLoginAttemptCommand(
        @NotBlank String issuer,
        @NotNull ProofType proofType,
        @NotEmpty Map<@NotBlank String, @NotBlank String> proofParameters
) {
    public ExternalLoginAttemptCommand {
        proofParameters =
                proofParameters == null ? null : Map.copyOf(proofParameters);
    }
}
