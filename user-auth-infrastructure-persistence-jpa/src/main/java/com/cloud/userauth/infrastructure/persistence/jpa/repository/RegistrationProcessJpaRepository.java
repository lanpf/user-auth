package com.cloud.userauth.infrastructure.persistence.jpa.repository;
import com.cloud.userauth.infrastructure.persistence.jpa.model.RegistrationProcessDO;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface RegistrationProcessJpaRepository extends JpaRepository<RegistrationProcessDO, Long> {
    Optional<RegistrationProcessDO> findByChallengeId(Long challengeId);
}
