package com.cloud.userauth.infrastructure.persistence.jpa.repository;
import com.cloud.userauth.infrastructure.persistence.jpa.model.CredentialDO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CredentialJpaRepository extends JpaRepository<CredentialDO, Long> {
    List<CredentialDO> findByAuthAccountIdOrderByCreatedAt(Long accountId);
    Optional<CredentialDO> findFirstByCredentialTypeAndIssuerAndPrincipalAndStatus(String type, String issuer, String principal, String status);
    boolean existsByCredentialTypeAndIssuerAndPrincipalAndStatus(String type, String issuer, String principal, String status);
}
