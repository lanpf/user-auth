package com.cloud.userauth.infrastructure.persistence.jpa.repository;
import com.cloud.userauth.domain.authentication.challenge.*;
import com.cloud.userauth.infrastructure.persistence.jpa.mapper.UserAuthPersistenceMapper;
import com.cloud.userauth.infrastructure.persistence.repository.AuthChallengePersistenceRepository;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
@RequiredArgsConstructor
public class AuthChallengeJpaPersistenceRepository implements AuthChallengePersistenceRepository {
    private final AuthChallengeJpaRepository repository; private final UserAuthPersistenceMapper mapper;
    @Override public void save(AuthChallenge value) { repository.save(mapper.toDataObject(value)); }
    @Override public Optional<AuthChallenge> findById(AuthChallengeId id) { return repository.findById(id.value()).map(mapper::toDomain); }
    @Override
    public Optional<AuthChallenge> findReusable(
            AuthChallengeType type,
            ChallengeTarget target,
            AuthChallengeScene scene,
            Instant now
    ) {
        return repository
                .findFirstByChallengeTypeAndTargetAndSceneAndStatusAndReusableUntilAfterAndExpiresAtAfterOrderByCreatedAtDesc(
                        type.name(),
                        target.value(),
                        scene.name(),
                        AuthChallengeStatus.ISSUED.name(),
                        now,
                        now)
                .map(mapper::toDomain);
    }
}
