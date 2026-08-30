package com.cloud.userauth.infrastructure.persistence.jpa.config;

import com.cloud.userauth.infrastructure.persistence.jpa.mapper.DomainEventPersistenceMapper;
import com.cloud.userauth.infrastructure.persistence.jpa.repository.AuthAccountJpaPersistenceRepository;
import com.cloud.userauth.infrastructure.persistence.jpa.repository.AuthAccountJpaRepository;
import com.cloud.userauth.infrastructure.persistence.jpa.repository.AuthChallengeJpaPersistenceRepository;
import com.cloud.userauth.infrastructure.persistence.jpa.repository.AuthChallengeJpaRepository;
import com.cloud.userauth.infrastructure.persistence.jpa.repository.AuthorizationJpaPersistenceRepository;
import com.cloud.userauth.infrastructure.persistence.jpa.repository.ChannelAuthorizationPolicyJpaRepository;
import com.cloud.userauth.infrastructure.persistence.jpa.repository.ChannelAuthorizationPolicyPermissionJpaRepository;
import com.cloud.userauth.infrastructure.persistence.jpa.repository.ChannelAuthorizationPolicyRoleJpaRepository;
import com.cloud.userauth.infrastructure.persistence.jpa.repository.CredentialJpaRepository;
import com.cloud.userauth.infrastructure.persistence.jpa.repository.DomainEventJpaPersistenceRepository;
import com.cloud.userauth.infrastructure.persistence.jpa.repository.DomainEventJpaRepository;
import com.cloud.userauth.infrastructure.persistence.jpa.repository.LoginAttemptJpaPersistenceRepository;
import com.cloud.userauth.infrastructure.persistence.jpa.repository.LoginAttemptJpaRepository;
import com.cloud.userauth.infrastructure.persistence.jpa.repository.LoginSessionJpaPersistenceRepository;
import com.cloud.userauth.infrastructure.persistence.jpa.repository.LoginSessionJpaRepository;
import com.cloud.userauth.infrastructure.persistence.jpa.repository.RegistrationProcessJpaPersistenceRepository;
import com.cloud.userauth.infrastructure.persistence.jpa.repository.RegistrationProcessJpaRepository;
import com.cloud.userauth.infrastructure.persistence.jpa.repository.PermissionJpaRepository;
import com.cloud.userauth.infrastructure.persistence.jpa.repository.RoleJpaRepository;
import com.cloud.userauth.infrastructure.persistence.jpa.repository.RolePermissionJpaRepository;
import com.cloud.userauth.infrastructure.persistence.jpa.repository.UserChannelPolicyApplicationJpaRepository;
import com.cloud.userauth.infrastructure.persistence.jpa.repository.UserPermissionGrantJpaRepository;
import com.cloud.userauth.infrastructure.persistence.jpa.repository.UserRoleGrantJpaRepository;
import com.cloud.userauth.infrastructure.persistence.jpa.mapper.AuthorizationPersistenceMapper;
import com.cloud.userauth.infrastructure.persistence.jpa.mapper.UserAuthPersistenceMapper;
import com.cloud.userauth.infrastructure.persistence.jpa.model.*;
import com.cloud.userauth.infrastructure.persistence.repository.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EntityScan(basePackageClasses = {AuthAccountDO.class, PermissionDO.class, DomainEventDO.class})
public class JpaPersistenceConfiguration {
    @Bean @ConditionalOnMissingBean
    AuthAccountPersistenceRepository authAccountPersistenceRepository(AuthAccountJpaRepository a, CredentialJpaRepository c, UserAuthPersistenceMapper m) { return new AuthAccountJpaPersistenceRepository(a, c, m); }
    @Bean @ConditionalOnMissingBean
    AuthChallengePersistenceRepository authChallengePersistenceRepository(AuthChallengeJpaRepository r, UserAuthPersistenceMapper m) { return new AuthChallengeJpaPersistenceRepository(r, m); }
    @Bean @ConditionalOnMissingBean
    RegistrationProcessPersistenceRepository registrationProcessPersistenceRepository(RegistrationProcessJpaRepository r, UserAuthPersistenceMapper m) { return new RegistrationProcessJpaPersistenceRepository(r, m); }
    @Bean @ConditionalOnMissingBean
    LoginSessionPersistenceRepository loginSessionPersistenceRepository(LoginSessionJpaRepository r, UserAuthPersistenceMapper m) { return new LoginSessionJpaPersistenceRepository(r, m); }
    @Bean @ConditionalOnMissingBean
    LoginAttemptPersistenceRepository loginAttemptPersistenceRepository(LoginAttemptJpaRepository r, UserAuthPersistenceMapper m) { return new LoginAttemptJpaPersistenceRepository(r, m); }
    @Bean @ConditionalOnMissingBean
    DomainEventJpaPersistenceRepository storedDomainEventPersistenceRepository(DomainEventJpaRepository r, DomainEventPersistenceMapper m) { return new DomainEventJpaPersistenceRepository(r, m); }

    @Bean
    @ConditionalOnMissingBean
    AuthorizationJpaPersistenceRepository authorizationJpaPersistenceRepository(
            RoleJpaRepository roleRepository,
            RolePermissionJpaRepository rolePermissionRepository,
            PermissionJpaRepository permissionRepository,
            UserRoleGrantJpaRepository roleGrantRepository,
            UserPermissionGrantJpaRepository permissionGrantRepository,
            ChannelAuthorizationPolicyJpaRepository policyRepository,
            ChannelAuthorizationPolicyRoleJpaRepository policyRoleRepository,
            ChannelAuthorizationPolicyPermissionJpaRepository policyPermissionRepository,
            UserChannelPolicyApplicationJpaRepository applicationRepository,
            AuthorizationPersistenceMapper mapper
    ) {
        return new AuthorizationJpaPersistenceRepository(
                roleRepository,
                rolePermissionRepository,
                permissionRepository,
                roleGrantRepository,
                permissionGrantRepository,
                policyRepository,
                policyRoleRepository,
                policyPermissionRepository,
                applicationRepository,
                mapper);
    }
}
