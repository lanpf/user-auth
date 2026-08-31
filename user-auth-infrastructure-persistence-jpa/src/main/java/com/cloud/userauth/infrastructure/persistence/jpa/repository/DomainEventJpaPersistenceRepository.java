package com.cloud.userauth.infrastructure.persistence.jpa.repository;

import java.util.List;

import com.cloud.framework.starter.domain.eventstore.persistence.DomainEventEnvelope;
import com.cloud.framework.starter.domain.eventstore.persistence.DomainEventEnvelopePersistenceRepository;
import com.cloud.userauth.infrastructure.persistence.jpa.mapper.DomainEventPersistenceMapper;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DomainEventJpaPersistenceRepository implements DomainEventEnvelopePersistenceRepository {
    private final DomainEventJpaRepository repository;
    private final DomainEventPersistenceMapper mapper;

    @Override
    public void saveAll(List<DomainEventEnvelope> domainEventEnvelopes) {
        repository.saveAll(domainEventEnvelopes.stream().map(mapper::toDataObject).toList());
    }
}
