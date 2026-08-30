package com.cloud.userauth.infrastructure.id;

import com.cloud.framework.domain.DomainEventId;
import com.cloud.framework.domain.DomainEventIdGenerator;
import com.cloud.framework.id.LongIdGenerator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DomainEventIdGeneratorAdapter implements DomainEventIdGenerator {
    private final LongIdGenerator idGenerator;

    @Override
    public DomainEventId nextId() {
        return new DomainEventId(idGenerator.nextId(IdGeneratorScene.DOMAIN_EVENT.getValue()));
    }
}
