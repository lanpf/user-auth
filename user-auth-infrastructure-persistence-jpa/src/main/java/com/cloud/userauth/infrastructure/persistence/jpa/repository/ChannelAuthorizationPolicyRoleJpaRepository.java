package com.cloud.userauth.infrastructure.persistence.jpa.repository;

import com.cloud.userauth.infrastructure.persistence.jpa.model.ChannelAuthorizationPolicyRoleDO;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChannelAuthorizationPolicyRoleJpaRepository extends JpaRepository<
        ChannelAuthorizationPolicyRoleDO,
        ChannelAuthorizationPolicyRoleDO.Key> {
    List<ChannelAuthorizationPolicyRoleDO> findByChannelCodeOrderByRoleCodeAsc(String channelCode);

    void deleteByChannelCode(String channelCode);
}
