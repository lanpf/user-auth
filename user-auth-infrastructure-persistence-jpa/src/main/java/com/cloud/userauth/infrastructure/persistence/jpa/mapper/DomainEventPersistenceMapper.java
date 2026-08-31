package com.cloud.userauth.infrastructure.persistence.jpa.mapper;

import com.cloud.framework.starter.domain.eventstore.persistence.DomainEventEnvelope;
import com.cloud.userauth.infrastructure.persistence.jpa.model.DomainEventDO;

public interface DomainEventPersistenceMapper {

    DomainEventDO toDataObject(DomainEventEnvelope source);
}
