package com.cloud.userauth.infrastructure.repository.adapter;

import com.cloud.framework.id.LongIdGenerator;
import com.cloud.userauth.domain.authentication.challenge.*;
import com.cloud.userauth.infrastructure.id.IdGeneratorNames;
import com.cloud.userauth.infrastructure.persistence.repository.AuthChallengePersistenceRepository;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AuthChallengeRepositoryAdapter implements AuthChallengeRepository {
    private final LongIdGenerator idGenerator;
    private final AuthChallengePersistenceRepository persistenceRepository;
    @Override public AuthChallengeId nextId() { return new AuthChallengeId(idGenerator.nextId(IdGeneratorNames.AUTH_CHALLENGE)); }
    @Override public void save(AuthChallenge aggregate) { persistenceRepository.save(aggregate); }
    @Override public Optional<AuthChallenge> findById(AuthChallengeId id) { return persistenceRepository.findById(id); }
    @Override
    public Optional<AuthChallenge> findReusable(
            AuthChallengeType type,
            ChallengeTarget target,
            AuthChallengeScene scene,
            Instant now
    ) {
        return persistenceRepository.findReusable(type, target, scene, now);
    }
}
