package com.cloud.userauth.infrastructure.persistence.jpa.repository;

import com.cloud.userauth.infrastructure.persistence.jpa.model.ChannelAuthorizationPolicyPermissionDO;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChannelAuthorizationPolicyPermissionJpaRepository extends JpaRepository<
        ChannelAuthorizationPolicyPermissionDO,
        ChannelAuthorizationPolicyPermissionDO.Key> {
    List<ChannelAuthorizationPolicyPermissionDO> findByChannelCodeOrderByPermissionCodeAsc(
            String channelCode);

    void deleteByChannelCode(String channelCode);
}
