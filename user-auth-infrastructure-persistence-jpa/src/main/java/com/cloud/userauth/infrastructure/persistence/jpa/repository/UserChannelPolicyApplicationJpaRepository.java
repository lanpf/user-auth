package com.cloud.userauth.infrastructure.persistence.jpa.repository;

import com.cloud.userauth.infrastructure.persistence.jpa.model.UserChannelPolicyApplicationDO;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserChannelPolicyApplicationJpaRepository extends JpaRepository<
        UserChannelPolicyApplicationDO,
        UserChannelPolicyApplicationDO.Key> {
    List<UserChannelPolicyApplicationDO> findByChannelCodeAndAppliedVersionLessThanOrderByUserIdAsc(
            String channelCode, Long policyVersion, Pageable pageable);
}
