package com.cloud.userauth.infrastructure.persistence.jpa.repository;

import com.cloud.userauth.infrastructure.persistence.jpa.model.RolePermissionDO;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolePermissionJpaRepository extends JpaRepository<RolePermissionDO, RolePermissionDO.Key> {
    List<RolePermissionDO> findByRoleCodeOrderByPermissionCodeAsc(String roleCode);

    void deleteByRoleCode(String roleCode);
}
