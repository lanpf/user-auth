package com.cloud.userauth.infrastructure.persistence.jpa.repository;

import com.cloud.userauth.infrastructure.persistence.jpa.model.LoginAttemptDO;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginAttemptJpaRepository extends JpaRepository<LoginAttemptDO, String> {
    Optional<LoginAttemptDO> findFirstByIssuerAndPrincipalAndStatusInOrderByCreatedAtDesc(
            String issuer,
            String principal,
            Iterable<String> statuses
    );
}
