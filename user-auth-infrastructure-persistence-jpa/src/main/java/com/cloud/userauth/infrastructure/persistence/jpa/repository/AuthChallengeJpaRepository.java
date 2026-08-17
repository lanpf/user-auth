package com.cloud.userauth.infrastructure.persistence.jpa.repository;

import com.cloud.userauth.infrastructure.persistence.jpa.model.AuthChallengeDO;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthChallengeJpaRepository extends JpaRepository<AuthChallengeDO, Long> {
    Optional<AuthChallengeDO>
            findFirstByChallengeTypeAndTargetAndSceneAndStatusAndReusableUntilAfterAndExpiresAtAfterOrderByCreatedAtDesc(
                    String type,
                    String target,
                    String scene,
                    String status,
                    Instant reusableAt,
                    Instant unexpiredAt);
}
