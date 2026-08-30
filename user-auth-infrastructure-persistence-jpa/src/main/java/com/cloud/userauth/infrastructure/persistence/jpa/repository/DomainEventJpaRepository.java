package com.cloud.userauth.infrastructure.persistence.jpa.repository;

import com.cloud.userauth.infrastructure.persistence.jpa.model.DomainEventDO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DomainEventJpaRepository extends JpaRepository<DomainEventDO, Long> {
}
