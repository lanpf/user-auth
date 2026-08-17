package com.cloud.userauth.infrastructure.persistence.jpa.repository;

import com.cloud.userauth.infrastructure.persistence.jpa.model.UserPermissionGrantDO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPermissionGrantJpaRepository extends JpaRepository<UserPermissionGrantDO, Long> {
    List<UserPermissionGrantDO> findByUserIdAndStatus(Long userId, String status);
    Optional<UserPermissionGrantDO> findByUserIdAndPermissionCodeAndSourceTypeAndSourceIdAndStatus(
            Long userId, String permissionCode, String sourceType, String sourceId, String status);
    List<UserPermissionGrantDO> findByUserIdAndSourceTypeAndSourceIdAndStatus(
            Long userId, String sourceType, String sourceId, String status);
}
