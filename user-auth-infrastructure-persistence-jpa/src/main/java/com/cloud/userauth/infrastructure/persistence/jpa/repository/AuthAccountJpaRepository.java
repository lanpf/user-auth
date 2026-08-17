package com.cloud.userauth.infrastructure.persistence.jpa.repository;
import com.cloud.userauth.infrastructure.persistence.jpa.model.AuthAccountDO;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AuthAccountJpaRepository extends JpaRepository<AuthAccountDO, Long> {
    Optional<AuthAccountDO> findByUserId(Long userId);
    boolean existsByUserIdAndStatus(Long userId, String status);
}
