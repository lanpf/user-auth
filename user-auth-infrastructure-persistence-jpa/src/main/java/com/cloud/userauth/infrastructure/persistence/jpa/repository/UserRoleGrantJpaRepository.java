package com.cloud.userauth.infrastructure.persistence.jpa.repository;

import com.cloud.userauth.infrastructure.persistence.jpa.model.UserRoleGrantDO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleGrantJpaRepository extends JpaRepository<UserRoleGrantDO, Long> {
    List<UserRoleGrantDO> findByUserIdAndStatus(Long userId, String status);
    Optional<UserRoleGrantDO> findByUserIdAndRoleCodeAndSourceTypeAndSourceIdAndStatus(
            Long userId, String roleCode, String sourceType, String sourceId, String status);
    List<UserRoleGrantDO> findByUserIdAndSourceTypeAndSourceIdAndStatus(
            Long userId, String sourceType, String sourceId, String status);
}
