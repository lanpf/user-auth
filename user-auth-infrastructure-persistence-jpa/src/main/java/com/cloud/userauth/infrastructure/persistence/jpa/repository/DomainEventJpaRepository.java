package com.cloud.userauth.infrastructure.persistence.jpa.repository;

import com.cloud.framework.starter.domain.eventstore.persistence.DomainEventDO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DomainEventJpaRepository extends JpaRepository<DomainEventDO, Long> {
}
