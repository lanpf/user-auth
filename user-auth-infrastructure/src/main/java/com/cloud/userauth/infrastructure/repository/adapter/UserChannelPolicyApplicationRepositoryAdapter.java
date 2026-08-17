package com.cloud.userauth.infrastructure.repository.adapter;

import com.cloud.userauth.domain.authorization.ChannelCode;
import com.cloud.userauth.domain.authorization.UserChannelPolicyApplication;
import com.cloud.userauth.domain.authorization.UserChannelPolicyApplicationRepository;
import com.cloud.userauth.domain.user.UserId;
import com.cloud.userauth.infrastructure.persistence.repository.UserChannelPolicyApplicationPersistenceRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserChannelPolicyApplicationRepositoryAdapter implements UserChannelPolicyApplicationRepository {
    private final UserChannelPolicyApplicationPersistenceRepository persistenceRepository;
    @Override public void save(UserChannelPolicyApplication application) { persistenceRepository.save(application); }
    @Override public Optional<UserChannelPolicyApplication> findByUserIdAndChannelCode(UserId userId, ChannelCode channelCode) { return persistenceRepository.findByUserIdAndChannelCode(userId, channelCode); }
    @Override public List<UserChannelPolicyApplication> findPendingByChannelCodeAndPolicyVersion(
            ChannelCode channelCode, Long policyVersion, int batchSize
    ) {
        return persistenceRepository.findPendingApplicationsByChannelCodeAndPolicyVersion(
                channelCode, policyVersion, batchSize);
    }
}
