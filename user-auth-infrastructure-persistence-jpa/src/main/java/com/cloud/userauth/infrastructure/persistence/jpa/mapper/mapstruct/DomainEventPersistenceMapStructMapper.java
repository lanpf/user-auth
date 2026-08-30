package com.cloud.userauth.infrastructure.persistence.jpa.mapper.mapstruct;

import com.cloud.framework.core.mapper.MapStructConfig;
import com.cloud.framework.starter.domain.eventstore.persistence.StoredDomainEvent;
import com.cloud.userauth.infrastructure.persistence.jpa.mapper.DomainEventPersistenceMapper;
import com.cloud.userauth.infrastructure.persistence.jpa.model.DomainEventDO;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
public interface DomainEventPersistenceMapStructMapper extends DomainEventPersistenceMapper {

    @Override
    DomainEventDO toDataObject(StoredDomainEvent source);

}
