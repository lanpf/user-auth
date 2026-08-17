package com.cloud.userauth.infrastructure.persistence.repository;

import com.cloud.userauth.domain.authorization.ChannelCode;
import com.cloud.userauth.domain.authorization.UserChannelPolicyApplication;
import com.cloud.userauth.domain.user.UserId;
import java.util.List;
import java.util.Optional;

public interface UserChannelPolicyApplicationPersistenceRepository {
    void save(UserChannelPolicyApplication application);
    Optional<UserChannelPolicyApplication> findByUserIdAndChannelCode(
            UserId userId, ChannelCode channelCode);
    List<UserChannelPolicyApplication> findPendingApplicationsByChannelCodeAndPolicyVersion(
            ChannelCode channelCode, Long policyVersion, int batchSize);
}
