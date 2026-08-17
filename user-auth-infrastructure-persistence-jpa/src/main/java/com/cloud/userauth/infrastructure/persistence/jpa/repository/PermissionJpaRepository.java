package com.cloud.userauth.infrastructure.persistence.jpa.repository;

import com.cloud.userauth.infrastructure.persistence.jpa.model.PermissionDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PermissionJpaRepository extends JpaRepository<PermissionDO, String> {
    Page<PermissionDO> findAllByOrderByPermissionCodeAsc(Pageable pageable);
}
