package com.cloud.userauth.domain.authorization;

import com.cloud.userauth.domain.user.UserId;
import java.util.List;
import java.util.Optional;

public interface UserChannelPolicyApplicationRepository {
    void save(UserChannelPolicyApplication application);

    Optional<UserChannelPolicyApplication> findByUserIdAndChannelCode(
            UserId userId,
            ChannelCode channelCode);

    List<UserChannelPolicyApplication> findPendingByChannelCodeAndPolicyVersion(
            ChannelCode channelCode, Long policyVersion, int batchSize);
}
