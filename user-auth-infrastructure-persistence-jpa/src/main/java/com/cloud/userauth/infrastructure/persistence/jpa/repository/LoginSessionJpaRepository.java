package com.cloud.userauth.infrastructure.persistence.jpa.repository;
import com.cloud.userauth.infrastructure.persistence.jpa.model.LoginSessionDO;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface LoginSessionJpaRepository extends JpaRepository<LoginSessionDO, String> {
    List<LoginSessionDO> findByUserIdAndStatus(Long userId, String status);
    List<LoginSessionDO> findByUserIdAndDeviceIdAndStatus(Long userId, String deviceId, String status);
}
