package com.cloud.userauth.infrastructure.persistence.jpa.repository;

import com.cloud.framework.starter.domain.eventstore.persistence.DomainEventDO;
import com.cloud.framework.starter.domain.eventstore.persistence.DomainEventPersistenceRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DomainEventJpaPersistenceRepository implements DomainEventPersistenceRepository {
    private final DomainEventJpaRepository repository;

    @Override
    public void saveAll(List<DomainEventDO> domainEvents) {
        repository.saveAll(domainEvents);
    }
}
