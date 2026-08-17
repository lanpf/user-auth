package com.cloud.userauth.infrastructure.config;

import com.cloud.framework.domain.DomainEventIdGenerator;
import com.cloud.framework.domain.DomainEventStore;
import com.cloud.framework.id.LongIdGenerator;
import com.cloud.framework.lock.LockExecutor;
import com.cloud.userauth.application.challenge.AuthChallengeCommandService;
import com.cloud.userauth.application.authorization.AuthorizationQueryService;
import com.cloud.userauth.application.authorization.AuthorizationCatalogCommandService;
import com.cloud.userauth.application.authorization.ChannelAuthorizationPolicyCommandService;
import com.cloud.userauth.application.authorization.ChannelAuthorizationPolicySynchronizationService;
import com.cloud.userauth.application.authorization.UserChannelAuthorizationSynchronizer;
import com.cloud.userauth.application.login.MobileOtpAuthenticationProcess;
import com.cloud.userauth.application.login.MobileOtpLoginCommandService;
import com.cloud.userauth.application.login.MobileOtpLoginTransactionService;
import com.cloud.userauth.application.login.refresh.RefreshLoginCommandService;
import com.cloud.userauth.application.logout.LogoutCommandService;
import com.cloud.userauth.application.login.external.ExternalAuthenticationProcess;
import com.cloud.userauth.application.login.external.ExternalLoginCommandService;
import com.cloud.userauth.application.login.external.TrustedMobileAuthorizationCodeLoginCommandService;
import com.cloud.userauth.application.login.external.BoundExternalCredentialAuthorizationCodeLoginCommandService;
import com.cloud.userauth.application.login.external.ExternalLoginTransactionService;
import com.cloud.userauth.application.login.external.ExternalLoginAttemptCommandService;
import com.cloud.userauth.application.credential.BindExternalCredentialCommandService;
import com.cloud.userauth.application.port.AuthChallengeDispatcher;
import com.cloud.userauth.application.port.AuthChallengeIssueLock;
import com.cloud.userauth.application.port.AuthChallengePolicyProvider;
import com.cloud.userauth.application.port.MobileOtpLoginLock;
import com.cloud.userauth.application.port.LoginTokenIssuer;
import com.cloud.userauth.application.port.LoginTokenRefresher;
import com.cloud.userauth.application.port.ClientRenewalPolicyResolver;
import com.cloud.userauth.application.port.RefreshTokenRotationLock;
import com.cloud.userauth.application.port.OneTimeCodeGenerator;
import com.cloud.userauth.application.port.ChallengeSecretHasher;
import com.cloud.userauth.application.port.ExternalIdentityVerifier;
import com.cloud.userauth.application.port.ExternalIdentityVerifierRegistry;
import com.cloud.userauth.application.port.IssuerMobileTrustPolicyProvider;
import com.cloud.userauth.application.port.LoginSessionArtifactRevoker;
import com.cloud.userauth.application.port.UserGateway;
import com.cloud.userauth.application.registration.RegistrationProcessRepository;
import com.cloud.userauth.domain.authentication.account.AuthAccountRepository;
import com.cloud.userauth.domain.authentication.challenge.AuthChallengeRepository;
import com.cloud.userauth.domain.authentication.credential.CredentialIdGenerator;
import com.cloud.userauth.domain.authentication.service.AuthenticationDomainService;
import com.cloud.userauth.domain.authentication.service.CredentialDomainService;
import com.cloud.userauth.domain.authentication.service.ExternalIdentityDomainService;
import com.cloud.userauth.domain.authentication.service.SessionDomainService;
import com.cloud.userauth.domain.authentication.session.LoginSessionRepository;
import com.cloud.userauth.domain.authentication.loginattempt.LoginAttemptRepository;
import com.cloud.userauth.domain.authorization.ChannelAuthorizationPolicyRepository;
import com.cloud.userauth.domain.authorization.PermissionRepository;
import com.cloud.userauth.domain.authorization.RoleRepository;
import com.cloud.userauth.domain.authorization.UserChannelPolicyApplicationRepository;
import com.cloud.userauth.domain.authorization.UserPermissionGrantRepository;
import com.cloud.userauth.domain.authorization.UserRoleGrantRepository;
import com.cloud.userauth.domain.authorization.service.AuthorizationDomainService;
import com.cloud.userauth.domain.user.UserIdGenerator;
import com.cloud.userauth.infrastructure.challenge.PropertiesAuthChallengePolicyProvider;
import com.cloud.userauth.infrastructure.external.DefaultExternalIdentityVerifierRegistry;
import com.cloud.userauth.infrastructure.external.PropertiesIssuerMobileTrustPolicyProvider;
import com.cloud.userauth.infrastructure.id.CredentialIdGeneratorAdapter;
import com.cloud.userauth.infrastructure.id.DomainEventIdGeneratorAdapter;
import com.cloud.userauth.infrastructure.id.UserIdGeneratorAdapter;
import com.cloud.userauth.infrastructure.lock.RedisAuthChallengeIssueLock;
import com.cloud.userauth.infrastructure.lock.RedisMobileOtpLoginLock;
import com.cloud.userauth.infrastructure.lock.RedisRefreshTokenRotationLock;
import com.cloud.userauth.infrastructure.persistence.repository.AuthAccountPersistenceRepository;
import com.cloud.userauth.infrastructure.persistence.repository.AuthChallengePersistenceRepository;
import com.cloud.userauth.infrastructure.persistence.repository.LoginSessionPersistenceRepository;
import com.cloud.userauth.infrastructure.persistence.repository.LoginAttemptPersistenceRepository;
import com.cloud.userauth.infrastructure.persistence.repository.RegistrationProcessPersistenceRepository;
import com.cloud.userauth.infrastructure.persistence.repository.ChannelAuthorizationPolicyPersistenceRepository;
import com.cloud.userauth.infrastructure.persistence.repository.PermissionPersistenceRepository;
import com.cloud.userauth.infrastructure.persistence.repository.RolePersistenceRepository;
import com.cloud.userauth.infrastructure.persistence.repository.UserChannelPolicyApplicationPersistenceRepository;
import com.cloud.userauth.infrastructure.persistence.repository.UserPermissionGrantPersistenceRepository;
import com.cloud.userauth.infrastructure.persistence.repository.UserRoleGrantPersistenceRepository;
import com.cloud.userauth.infrastructure.repository.adapter.AuthAccountRepositoryAdapter;
import com.cloud.userauth.infrastructure.repository.adapter.AuthChallengeRepositoryAdapter;
import com.cloud.userauth.infrastructure.repository.adapter.LoginSessionRepositoryAdapter;
import com.cloud.userauth.infrastructure.repository.adapter.LoginAttemptRepositoryAdapter;
import com.cloud.userauth.infrastructure.repository.adapter.RegistrationProcessRepositoryAdapter;
import com.cloud.userauth.infrastructure.repository.adapter.ChannelAuthorizationPolicyRepositoryAdapter;
import com.cloud.userauth.infrastructure.repository.adapter.PermissionRepositoryAdapter;
import com.cloud.userauth.infrastructure.repository.adapter.RoleRepositoryAdapter;
import com.cloud.userauth.infrastructure.repository.adapter.UserChannelPolicyApplicationRepositoryAdapter;
import com.cloud.userauth.infrastructure.repository.adapter.UserPermissionGrantRepositoryAdapter;
import com.cloud.userauth.infrastructure.repository.adapter.UserRoleGrantRepositoryAdapter;
import com.cloud.userauth.infrastructure.security.HmacSha256ChallengeSecretHasher;
import java.time.Clock;
import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        AuthChallengeProperties.class,
        ClientAppRegistryProperties.class,
        ExternalIdentityProperties.class,
        AccessTokenProperties.class,
        LoginSessionProperties.class
})
public class UserAuthConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public ClientRenewalPolicyResolver clientRenewalPolicyResolver(
            ClientAppRegistryProperties properties
    ) {
        return new PropertiesClientRenewalPolicyResolver(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthAccountRepository authAccountRepository(
            LongIdGenerator idGenerator,
            AuthAccountPersistenceRepository persistenceRepository
    ) {
        return new AuthAccountRepositoryAdapter(idGenerator, persistenceRepository);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthChallengeRepository authChallengeRepository(
            LongIdGenerator idGenerator,
            AuthChallengePersistenceRepository persistenceRepository
    ) {
        return new AuthChallengeRepositoryAdapter(idGenerator, persistenceRepository);
    }

    @Bean
    @ConditionalOnMissingBean
    public RegistrationProcessRepository registrationProcessRepository(
            LongIdGenerator idGenerator,
            RegistrationProcessPersistenceRepository persistenceRepository
    ) {
        return new RegistrationProcessRepositoryAdapter(idGenerator, persistenceRepository);
    }

    @Bean
    @ConditionalOnMissingBean
    public LoginSessionRepository loginSessionRepository(LoginSessionPersistenceRepository persistenceRepository) {
        return new LoginSessionRepositoryAdapter(persistenceRepository);
    }

    @Bean
    @ConditionalOnMissingBean
    public LoginAttemptRepository loginAttemptRepository(
            LoginAttemptPersistenceRepository persistenceRepository
    ) {
        return new LoginAttemptRepositoryAdapter(persistenceRepository);
    }

    @Bean
    @ConditionalOnMissingBean
    public RoleRepository roleRepository(RolePersistenceRepository persistenceRepository) {
        return new RoleRepositoryAdapter(persistenceRepository);
    }

    @Bean
    @ConditionalOnMissingBean
    public PermissionRepository permissionRepository(
            PermissionPersistenceRepository persistenceRepository
    ) {
        return new PermissionRepositoryAdapter(persistenceRepository);
    }

    @Bean
    @ConditionalOnMissingBean
    public UserRoleGrantRepository userRoleGrantRepository(
            LongIdGenerator idGenerator,
            UserRoleGrantPersistenceRepository persistenceRepository
    ) {
        return new UserRoleGrantRepositoryAdapter(idGenerator, persistenceRepository);
    }

    @Bean
    @ConditionalOnMissingBean
    public UserPermissionGrantRepository userPermissionGrantRepository(
            LongIdGenerator idGenerator,
            UserPermissionGrantPersistenceRepository persistenceRepository
    ) {
        return new UserPermissionGrantRepositoryAdapter(idGenerator, persistenceRepository);
    }

    @Bean
    @ConditionalOnMissingBean
    public ChannelAuthorizationPolicyRepository channelAuthorizationPolicyRepository(
            ChannelAuthorizationPolicyPersistenceRepository persistenceRepository
    ) {
        return new ChannelAuthorizationPolicyRepositoryAdapter(persistenceRepository);
    }

    @Bean
    @ConditionalOnMissingBean
    public UserChannelPolicyApplicationRepository userChannelPolicyApplicationRepository(
            UserChannelPolicyApplicationPersistenceRepository persistenceRepository
    ) {
        return new UserChannelPolicyApplicationRepositoryAdapter(persistenceRepository);
    }

    @Bean
    @ConditionalOnMissingBean
    public UserIdGenerator userIdGenerator(LongIdGenerator idGenerator) {
        return new UserIdGeneratorAdapter(idGenerator);
    }

    @Bean
    @ConditionalOnMissingBean
    public CredentialIdGenerator credentialIdGenerator(LongIdGenerator idGenerator) {
        return new CredentialIdGeneratorAdapter(idGenerator);
    }

    @Bean
    @ConditionalOnMissingBean
    public ChallengeSecretHasher secretHasher(AuthChallengeProperties properties) {
        return new HmacSha256ChallengeSecretHasher(properties.getPepper());
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthChallengeIssueLock authChallengeIssueLock(LockExecutor lockExecutor) {
        return new RedisAuthChallengeIssueLock(lockExecutor);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthChallengePolicyProvider authChallengePolicyProvider(AuthChallengeProperties properties) {
        return new PropertiesAuthChallengePolicyProvider(properties);
    }


    @Bean
    @ConditionalOnMissingBean
    public AuthChallengeCommandService authChallengeCommandService(
            AuthChallengeRepository challengeRepository,
            OneTimeCodeGenerator codeGenerator,
            ChallengeSecretHasher secretHasher,
            AuthChallengeDispatcher dispatcher,
            AuthChallengeIssueLock issueLock,
            AuthChallengePolicyProvider policyProvider,
            Clock clock
    ) {
        return new AuthChallengeCommandService(
                challengeRepository,
                codeGenerator,
                secretHasher,
                dispatcher,
                issueLock,
                policyProvider,
                clock);
    }

    @Bean
    @ConditionalOnMissingBean
    public MobileOtpLoginLock mobileOtpLoginLock(LockExecutor lockExecutor) {
        return new RedisMobileOtpLoginLock(lockExecutor);
    }

    @Bean
    @ConditionalOnMissingBean
    public RefreshTokenRotationLock refreshTokenRotationLock(LockExecutor lockExecutor) {
        return new RedisRefreshTokenRotationLock(lockExecutor);
    }

    @Bean
    @ConditionalOnMissingBean
    public DomainEventIdGenerator domainEventIdGenerator(LongIdGenerator idGenerator) {
        return new DomainEventIdGeneratorAdapter(idGenerator);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthenticationDomainService authenticationDomainService(
            DomainEventIdGenerator domainEventIdGenerator
    ) {
        return new AuthenticationDomainService(domainEventIdGenerator);
    }

    @Bean
    @ConditionalOnMissingBean
    public SessionDomainService sessionDomainService(DomainEventIdGenerator domainEventIdGenerator) {
        return new SessionDomainService(domainEventIdGenerator);
    }

    @Bean
    @ConditionalOnMissingBean
    public CredentialDomainService credentialDomainService(
            AuthAccountRepository authAccountRepository,
            DomainEventIdGenerator domainEventIdGenerator
    ) {
        return new CredentialDomainService(authAccountRepository, domainEventIdGenerator);
    }

    @Bean
    @ConditionalOnMissingBean
    public ExternalIdentityDomainService externalIdentityDomainService(
            DomainEventIdGenerator domainEventIdGenerator
    ) {
        return new ExternalIdentityDomainService(domainEventIdGenerator);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthorizationDomainService authorizationDomainService(
            RoleRepository roleRepository,
            UserRoleGrantRepository roleGrantRepository,
            PermissionRepository permissionRepository,
            UserPermissionGrantRepository permissionGrantRepository,
            DomainEventIdGenerator domainEventIdGenerator
    ) {
        return new AuthorizationDomainService(
                roleRepository,
                roleGrantRepository,
                permissionRepository,
                permissionGrantRepository,
                domainEventIdGenerator);
    }

    @Bean
    @ConditionalOnMissingBean
    public ChannelAuthorizationPolicySynchronizationService
            channelAuthorizationPolicySynchronizationService(
            ChannelAuthorizationPolicyRepository policyRepository,
            UserChannelPolicyApplicationRepository applicationRepository,
            UserRoleGrantRepository roleGrantRepository,
            UserPermissionGrantRepository permissionGrantRepository,
            AuthorizationDomainService authorizationDomainService,
            DomainEventStore domainEventStore,
            Clock clock
    ) {
        return new ChannelAuthorizationPolicySynchronizationService(
                policyRepository,
                applicationRepository,
                roleGrantRepository,
                permissionGrantRepository,
                authorizationDomainService,
                domainEventStore,
                clock);
    }

    @Bean
    @ConditionalOnMissingBean
    public ChannelAuthorizationPolicyCommandService channelAuthorizationPolicyCommandService(
            ChannelAuthorizationPolicyRepository policyRepository,
            AuthorizationDomainService authorizationDomainService,
            ChannelAuthorizationPolicySynchronizationService synchronizationService,
            Clock clock
    ) {
        return new ChannelAuthorizationPolicyCommandService(
                policyRepository, authorizationDomainService, synchronizationService, clock);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthorizationCatalogCommandService authorizationCatalogCommandService(
            PermissionRepository permissionRepository,
            RoleRepository roleRepository,
            AuthorizationDomainService authorizationDomainService,
            Clock clock
    ) {
        return new AuthorizationCatalogCommandService(
                permissionRepository, roleRepository, authorizationDomainService, clock);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthorizationQueryService authorizationQueryService(
            ChannelAuthorizationPolicyRepository policyRepository,
            UserChannelPolicyApplicationRepository applicationRepository,
            UserRoleGrantRepository roleGrantRepository,
            UserPermissionGrantRepository permissionGrantRepository,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            Clock clock
    ) {
        return new AuthorizationQueryService(
                policyRepository,
                applicationRepository,
                roleGrantRepository,
                permissionGrantRepository,
                roleRepository,
                permissionRepository,
                clock);
    }

    @Bean
    @ConditionalOnMissingBean
    public ExternalIdentityVerifierRegistry externalIdentityVerifierRegistry(
            List<ExternalIdentityVerifier> verifiers
    ) {
        return new DefaultExternalIdentityVerifierRegistry(verifiers);
    }

    @Bean
    @ConditionalOnMissingBean
    public IssuerMobileTrustPolicyProvider issuerMobileTrustPolicyProvider(
            ExternalIdentityProperties properties
    ) {
        return new PropertiesIssuerMobileTrustPolicyProvider(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public ExternalLoginAttemptCommandService externalLoginAttemptCommandService(
            ExternalIdentityVerifierRegistry verifierRegistry,
            IssuerMobileTrustPolicyProvider trustPolicyProvider,
            AuthAccountRepository authAccountRepository,
            LoginAttemptRepository loginAttemptRepository,
            ExternalIdentityDomainService externalIdentityDomainService,
            DomainEventStore domainEventStore,
            Clock clock,
            ExternalIdentityProperties properties
    ) {
        return new ExternalLoginAttemptCommandService(
                verifierRegistry, trustPolicyProvider, authAccountRepository,
                loginAttemptRepository, externalIdentityDomainService,
                domainEventStore, clock, properties.getLoginAttempt().getTtl());
    }

    @Bean
    @ConditionalOnMissingBean
    public ExternalLoginTransactionService externalLoginTransactionService(
            LoginAttemptRepository loginAttemptRepository,
            AuthChallengeRepository authChallengeRepository,
            AuthAccountRepository authAccountRepository,
            LoginSessionRepository loginSessionRepository,
            UserIdGenerator userIdGenerator,
            CredentialIdGenerator credentialIdGenerator,
            ChallengeSecretHasher secretHasher,
            AuthenticationDomainService authenticationDomainService,
            CredentialDomainService credentialDomainService,
            ExternalIdentityDomainService externalIdentityDomainService,
            DomainEventStore domainEventStore,
            Clock clock,
            LoginSessionProperties loginSessionProperties
    ) {
        return new ExternalLoginTransactionService(
                loginAttemptRepository, authChallengeRepository, authAccountRepository,
                loginSessionRepository, userIdGenerator, credentialIdGenerator,
                secretHasher, authenticationDomainService, credentialDomainService,
                externalIdentityDomainService, domainEventStore, clock,
                loginSessionProperties.getTtl());
    }

    @Bean
    @ConditionalOnMissingBean
    public ExternalAuthenticationProcess externalAuthenticationProcess(
            ExternalLoginTransactionService transactionService,
            UserGateway userGateway,
            UserChannelAuthorizationSynchronizer userChannelAuthorizationSynchronizer
    ) {
        return new ExternalAuthenticationProcess(
                transactionService, userGateway, userChannelAuthorizationSynchronizer);
    }

    @Bean
    @ConditionalOnMissingBean
    public ExternalLoginCommandService externalLoginCommandService(
            LoginTokenIssuer loginTokenIssuer
    ) {
        return new ExternalLoginCommandService(loginTokenIssuer);
    }

    @Bean
    @ConditionalOnMissingBean
    public TrustedMobileAuthorizationCodeLoginCommandService trustedMobileAuthorizationCodeLoginCommandService(
            ExternalLoginAttemptCommandService externalLoginAttemptCommandService,
            LoginTokenIssuer loginTokenIssuer
    ) {
        return new TrustedMobileAuthorizationCodeLoginCommandService(externalLoginAttemptCommandService, loginTokenIssuer);
    }

    @Bean
    @ConditionalOnMissingBean
    public BoundExternalCredentialAuthorizationCodeLoginCommandService boundExternalCredentialAuthorizationCodeLoginCommandService(
            ExternalLoginAttemptCommandService externalLoginAttemptCommandService,
            ExternalLoginCommandService externalLoginCommandService,
            ClientRenewalPolicyResolver renewalPolicyResolver
    ) {
        return new BoundExternalCredentialAuthorizationCodeLoginCommandService(
                externalLoginAttemptCommandService, externalLoginCommandService,
                renewalPolicyResolver);
    }

    @Bean
    @ConditionalOnMissingBean
    public BindExternalCredentialCommandService bindExternalCredentialCommandService(
            ExternalIdentityVerifierRegistry verifierRegistry,
            AuthAccountRepository authAccountRepository,
            CredentialIdGenerator credentialIdGenerator,
            CredentialDomainService credentialDomainService,
            DomainEventStore domainEventStore,
            Clock clock
    ) {
        return new BindExternalCredentialCommandService(
                verifierRegistry, authAccountRepository, credentialIdGenerator, credentialDomainService,
                domainEventStore, clock);
    }


    @Bean
    @ConditionalOnMissingBean
    public MobileOtpLoginTransactionService mobileOtpLoginTransactionService(
            AuthChallengeRepository challengeRepository,
            RegistrationProcessRepository registrationRepository,
            AuthAccountRepository authAccountRepository,
            LoginSessionRepository sessionRepository,
            UserIdGenerator userIdGenerator,
            CredentialIdGenerator credentialIdGenerator,
            ChallengeSecretHasher secretHasher,
            AuthenticationDomainService authenticationDomainService,
            DomainEventStore domainEventStore,
            Clock clock,
            LoginSessionProperties loginSessionProperties
    ) {
        return new MobileOtpLoginTransactionService(
                challengeRepository, registrationRepository, authAccountRepository,
                sessionRepository, userIdGenerator, credentialIdGenerator,
                secretHasher, authenticationDomainService, domainEventStore,
                clock,
                loginSessionProperties.getTtl());
    }

    @Bean
    @ConditionalOnMissingBean
    public MobileOtpAuthenticationProcess mobileOtpAuthenticationProcess(
            AuthChallengeRepository challengeRepository,
            RegistrationProcessRepository registrationRepository,
            AuthAccountRepository authAccountRepository,
            UserGateway userGateway,
            MobileOtpLoginLock mobileOtpLoginLock,
            MobileOtpLoginTransactionService mobileOtpLoginTransactionService,
            UserChannelAuthorizationSynchronizer userChannelAuthorizationSynchronizer
    ) {
        return new MobileOtpAuthenticationProcess(
                challengeRepository, registrationRepository, authAccountRepository,
                userGateway, mobileOtpLoginLock, mobileOtpLoginTransactionService,
                userChannelAuthorizationSynchronizer
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public MobileOtpLoginCommandService mobileOtpLoginCommandService(
            LoginTokenIssuer loginTokenIssuer
    ) {
        return new MobileOtpLoginCommandService(loginTokenIssuer);
    }

    @Bean
    @ConditionalOnMissingBean
    public RefreshLoginCommandService refreshLoginCommandService(
            ClientRenewalPolicyResolver renewalPolicyResolver,
            LoginTokenRefresher loginTokenRefresher
    ) {
        return new RefreshLoginCommandService(renewalPolicyResolver, loginTokenRefresher);
    }

    @Bean
    @ConditionalOnMissingBean
    public LogoutCommandService logoutCommandService(
            LoginSessionRepository sessionRepository,
            List<LoginSessionArtifactRevoker> artifactRevokers,
            SessionDomainService sessionDomainService,
            DomainEventStore domainEventStore,
            Clock clock
    ) {
        return new LogoutCommandService(
                sessionRepository, artifactRevokers, sessionDomainService, domainEventStore, clock);
    }
}
