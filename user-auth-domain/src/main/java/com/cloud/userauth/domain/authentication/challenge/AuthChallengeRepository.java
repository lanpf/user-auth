package com.cloud.userauth.domain.authentication.challenge;

import com.cloud.framework.domain.Repository;
import java.time.Instant;
import java.util.Optional;

public interface AuthChallengeRepository extends Repository<AuthChallenge, AuthChallengeId> {
    Optional<AuthChallenge> findReusable(
            AuthChallengeType type,
            ChallengeTarget target,
            AuthChallengeScene scene,
            Instant now
    );
}
