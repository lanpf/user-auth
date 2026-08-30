package com.cloud.userauth.infrastructure.persistence.jpa.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
public class DomainEventDO {
    @Id
    private Long eventId;

    private String eventType;

    private Instant occurredAt;

    private String payload;
}
