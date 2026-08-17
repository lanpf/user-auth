package com.cloud.userauth.infrastructure.persistence.repository;

import com.cloud.userauth.domain.authorization.ChannelAuthorizationPolicy;
import com.cloud.userauth.domain.authorization.ChannelCode;
import java.util.List;
import java.util.Optional;

public interface ChannelAuthorizationPolicyPersistenceRepository {
    void save(ChannelAuthorizationPolicy policy);
    Optional<ChannelAuthorizationPolicy> findByChannelCode(ChannelCode channelCode);
    boolean updateIfVersionMatches(ChannelAuthorizationPolicy policy, Long expectedVersion);
    List<ChannelAuthorizationPolicy> findAll();
}
