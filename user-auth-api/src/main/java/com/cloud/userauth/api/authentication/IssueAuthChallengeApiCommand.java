package com.cloud.userauth.api.authentication;

import com.cloud.framework.core.Request;
import com.cloud.userauth.api.enums.AuthChallengeSceneApiEnum;
import com.cloud.userauth.api.enums.AuthChallengeTypeApiEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record IssueAuthChallengeApiCommand(
        @NotNull AuthChallengeTypeApiEnum challengeType,
        @NotBlank String target,
        @NotNull AuthChallengeSceneApiEnum scene,
        @NotBlank String clientAppId,
        String clientPlatform,
        String clientVersion,
        @NotBlank String channelCode
) implements Request {
}
