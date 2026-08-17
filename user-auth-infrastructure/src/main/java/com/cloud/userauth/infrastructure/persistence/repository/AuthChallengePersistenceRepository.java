package com.cloud.userauth.infrastructure.persistence.repository;

import com.cloud.userauth.domain.authentication.challenge.AuthChallenge;
import com.cloud.userauth.domain.authentication.challenge.AuthChallengeId;
import com.cloud.userauth.domain.authentication.challenge.AuthChallengeScene;
import com.cloud.userauth.domain.authentication.challenge.AuthChallengeType;
import com.cloud.userauth.domain.authentication.challenge.ChallengeTarget;
import java.time.Instant;
import java.util.Optional;

public interface AuthChallengePersistenceRepository {
    void save(AuthChallenge challenge);
    Optional<AuthChallenge> findById(AuthChallengeId id);
    Optional<AuthChallenge> findReusable(AuthChallengeType type, ChallengeTarget target,
                                         AuthChallengeScene scene, Instant now);
}
