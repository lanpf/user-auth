package com.cloud.userauth.infrastructure.persistence.jpa.repository;

import com.cloud.userauth.infrastructure.persistence.jpa.model.ChannelAuthorizationPolicyDO;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChannelAuthorizationPolicyJpaRepository
        extends JpaRepository<ChannelAuthorizationPolicyDO, String> {
    @Modifying
    @Query("update ChannelAuthorizationPolicyDO policy set policy.status = :status, "
            + "policy.version = :nextVersion, policy.updatedAt = :updatedAt, "
            + "policy.activatedAt = :activatedAt where policy.channelCode = :channelCode "
            + "and policy.version = :expectedVersion")
    int updateByChannelCodeAndVersion(
            @Param("channelCode") String channelCode,
            @Param("expectedVersion") Long expectedVersion,
            @Param("status") String status,
            @Param("nextVersion") Long nextVersion,
            @Param("updatedAt") java.time.Instant updatedAt,
            @Param("activatedAt") java.time.Instant activatedAt);
}
