package com.cloud.userauth.infrastructure.repository.adapter;

import com.cloud.userauth.domain.authorization.ChannelAuthorizationPolicy;
import com.cloud.userauth.domain.authorization.ChannelAuthorizationPolicyRepository;
import com.cloud.userauth.domain.authorization.ChannelCode;
import com.cloud.userauth.infrastructure.persistence.repository.ChannelAuthorizationPolicyPersistenceRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ChannelAuthorizationPolicyRepositoryAdapter implements ChannelAuthorizationPolicyRepository {
    private final ChannelAuthorizationPolicyPersistenceRepository persistenceRepository;
    @Override public void save(ChannelAuthorizationPolicy policy) { persistenceRepository.save(policy); }
    @Override public Optional<ChannelAuthorizationPolicy> findByChannelCode(ChannelCode code) { return persistenceRepository.findByChannelCode(code); }
    @Override public boolean updateIfVersionMatches(ChannelAuthorizationPolicy policy, Long expectedVersion) { return persistenceRepository.updateIfVersionMatches(policy, expectedVersion); }
    @Override public List<ChannelAuthorizationPolicy> findAll() { return persistenceRepository.findAll(); }
}
