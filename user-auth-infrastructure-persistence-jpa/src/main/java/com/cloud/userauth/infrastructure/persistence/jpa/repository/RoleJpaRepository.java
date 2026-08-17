package com.cloud.userauth.infrastructure.persistence.jpa.repository;

import com.cloud.userauth.infrastructure.persistence.jpa.model.RoleDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RoleJpaRepository extends JpaRepository<RoleDO, String> {
    Page<RoleDO> findAllByOrderByRoleCodeAsc(Pageable pageable);
}
