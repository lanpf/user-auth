package com.cloud.userauth.infrastructure.persistence.jpa.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class ChannelAuthorizationPolicyDO {
    @Id
    private String channelCode;
    private String status;
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant activatedAt;
}
