package com.cloud.userauth.application.port;

import com.cloud.userauth.domain.authentication.challenge.AuthChallengeId;
import com.cloud.userauth.domain.authentication.challenge.ChallengeSecretHash;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public interface ChallengeSecretHasher {
    ChallengeSecretHash hash(@NotNull AuthChallengeId challengeId, @NotBlank String secret);
}
