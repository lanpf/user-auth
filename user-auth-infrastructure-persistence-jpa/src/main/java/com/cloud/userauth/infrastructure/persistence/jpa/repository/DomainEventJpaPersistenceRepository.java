package com.cloud.userauth.infrastructure.persistence.jpa.repository;

import java.util.List;

import com.cloud.framework.starter.domain.eventstore.persistence.StoredDomainEventPersistenceRepository;
import com.cloud.framework.starter.domain.eventstore.persistence.StoredDomainEvent;
import com.cloud.userauth.infrastructure.persistence.jpa.mapper.DomainEventPersistenceMapper;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DomainEventJpaPersistenceRepository implements StoredDomainEventPersistenceRepository {
    private final DomainEventJpaRepository repository;
    private final DomainEventPersistenceMapper mapper;

    @Override
    public void saveAll(List<StoredDomainEvent> storedDomainEvents) {
        repository.saveAll(storedDomainEvents.stream().map(mapper::toDataObject).toList());
    }
}
