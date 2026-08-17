package com.cloud.userauth.domain.authorization;

import com.cloud.userauth.domain.user.UserId;
import java.time.Instant;
import lombok.Getter;

@Getter
public class UserChannelPolicyApplication {
    private final UserId userId;
    private final ChannelCode channelCode;
    private Long appliedVersion;
    private final Instant firstAppliedAt;
    private Instant lastAppliedAt;

    private UserChannelPolicyApplication(
            UserId userId,
            ChannelCode channelCode,
            Long appliedVersion,
            Instant firstAppliedAt,
            Instant lastAppliedAt
    ) {
        this.userId = userId;
        this.channelCode = channelCode;
        this.appliedVersion = appliedVersion;
        this.firstAppliedAt = firstAppliedAt;
        this.lastAppliedAt = lastAppliedAt;
    }

    public static UserChannelPolicyApplication firstApplied(
            UserId userId,
            ChannelCode channelCode,
            Long policyVersion,
            Instant appliedAt
    ) {
        return new UserChannelPolicyApplication(
                userId, channelCode, policyVersion, appliedAt, appliedAt);
    }

    public static UserChannelPolicyApplication restore(
            UserId userId,
            ChannelCode channelCode,
            Long appliedVersion,
            Instant firstAppliedAt,
            Instant lastAppliedAt
    ) {
        return new UserChannelPolicyApplication(
                userId, channelCode, appliedVersion, firstAppliedAt, lastAppliedAt);
    }

    public void recordApplied(Long policyVersion, Instant appliedAt) {
        appliedVersion = policyVersion;
        lastAppliedAt = appliedAt;
    }
}
