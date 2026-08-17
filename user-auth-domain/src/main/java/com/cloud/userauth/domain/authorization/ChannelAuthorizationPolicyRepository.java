package com.cloud.userauth.domain.authorization;

import java.util.List;
import java.util.Optional;

public interface ChannelAuthorizationPolicyRepository {
    void save(ChannelAuthorizationPolicy policy);

    Optional<ChannelAuthorizationPolicy> findByChannelCode(ChannelCode channelCode);

    boolean updateIfVersionMatches(ChannelAuthorizationPolicy policy, Long expectedVersion);

    List<ChannelAuthorizationPolicy> findAll();
}
