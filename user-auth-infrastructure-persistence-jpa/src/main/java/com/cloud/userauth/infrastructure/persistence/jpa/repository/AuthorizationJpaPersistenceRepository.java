package com.cloud.userauth.infrastructure.persistence.jpa.repository;

import com.cloud.userauth.domain.authorization.ChannelAuthorizationPolicy;
import com.cloud.userauth.domain.authorization.ChannelCode;
import com.cloud.userauth.domain.authorization.GrantId;
import com.cloud.userauth.domain.authorization.GrantSource;
import com.cloud.userauth.domain.authorization.GrantStatus;
import com.cloud.userauth.domain.authorization.Permission;
import com.cloud.userauth.domain.authorization.PermissionCode;
import com.cloud.userauth.domain.authorization.Role;
import com.cloud.userauth.domain.authorization.RoleCode;
import com.cloud.userauth.domain.authorization.UserChannelPolicyApplication;
import com.cloud.userauth.domain.authorization.UserPermissionGrant;
import com.cloud.userauth.domain.authorization.UserRoleGrant;
import com.cloud.userauth.domain.user.UserId;
import com.cloud.userauth.infrastructure.persistence.jpa.mapper.AuthorizationPersistenceMapper;
import com.cloud.userauth.infrastructure.persistence.jpa.model.UserChannelPolicyApplicationDO;
import com.cloud.userauth.infrastructure.persistence.jpa.model.RoleDO;
import com.cloud.userauth.infrastructure.persistence.jpa.model.RolePermissionDO;
import com.cloud.userauth.infrastructure.persistence.jpa.model.ChannelAuthorizationPolicyDO;
import com.cloud.userauth.infrastructure.persistence.jpa.model.ChannelAuthorizationPolicyRoleDO;
import com.cloud.userauth.infrastructure.persistence.jpa.model.ChannelAuthorizationPolicyPermissionDO;
import com.cloud.userauth.infrastructure.persistence.repository.ChannelAuthorizationPolicyPersistenceRepository;
import com.cloud.userauth.infrastructure.persistence.repository.PermissionPersistenceRepository;
import com.cloud.userauth.infrastructure.persistence.repository.RolePersistenceRepository;
import com.cloud.userauth.infrastructure.persistence.repository.UserChannelPolicyApplicationPersistenceRepository;
import com.cloud.userauth.infrastructure.persistence.repository.UserPermissionGrantPersistenceRepository;
import com.cloud.userauth.infrastructure.persistence.repository.UserRoleGrantPersistenceRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import com.cloud.framework.domain.PagedList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AuthorizationJpaPersistenceRepository implements
        RolePersistenceRepository,
        PermissionPersistenceRepository,
        UserRoleGrantPersistenceRepository,
        UserPermissionGrantPersistenceRepository,
        ChannelAuthorizationPolicyPersistenceRepository,
        UserChannelPolicyApplicationPersistenceRepository {
    private static final String ACTIVE = GrantStatus.ACTIVE.name();

    private final RoleJpaRepository roleRepository;
    private final RolePermissionJpaRepository rolePermissionRepository;
    private final PermissionJpaRepository permissionRepository;
    private final UserRoleGrantJpaRepository roleGrantRepository;
    private final UserPermissionGrantJpaRepository permissionGrantRepository;
    private final ChannelAuthorizationPolicyJpaRepository policyRepository;
    private final ChannelAuthorizationPolicyRoleJpaRepository policyRoleRepository;
    private final ChannelAuthorizationPolicyPermissionJpaRepository policyPermissionRepository;
    private final UserChannelPolicyApplicationJpaRepository applicationRepository;
    private final AuthorizationPersistenceMapper mapper;

    @Override public void save(Role role) {
        roleRepository.save(mapper.toDataObject(role));
        rolePermissionRepository.deleteByRoleCode(role.getRoleCode().value());
        rolePermissionRepository.saveAll(role.permissionCodes().stream().map(permissionCode -> {
            RolePermissionDO detail = new RolePermissionDO();
            detail.setRoleCode(role.getRoleCode().value());
            detail.setPermissionCode(permissionCode.value());
            return detail;
        }).toList());
    }
    @Override public Optional<Role> findById(RoleCode code) {
        return roleRepository.findById(code.value()).map(this::toDomain);
    }
    @Override public PagedList<Role> findRoles(int pageNo, int pageSize) {
        Page<com.cloud.userauth.infrastructure.persistence.jpa.model.RoleDO> page =
                roleRepository.findAllByOrderByRoleCodeAsc(PageRequest.of(pageNo, pageSize));
        return new PagedList<>(page.getContent().stream().map(this::toDomain).toList(),
                page.getTotalElements());
    }
    @Override public void save(Permission permission) { permissionRepository.save(mapper.toDataObject(permission)); }
    @Override public Optional<Permission> findById(PermissionCode code) { return permissionRepository.findById(code.value()).map(mapper::toDomain); }
    @Override public List<Permission> findByIds(Collection<PermissionCode> codes) { return permissionRepository.findAllById(codes.stream().map(PermissionCode::value).toList()).stream().map(mapper::toDomain).toList(); }
    @Override public PagedList<Permission> findPermissions(int pageNo, int pageSize) {
        Page<com.cloud.userauth.infrastructure.persistence.jpa.model.PermissionDO> page =
                permissionRepository.findAllByOrderByPermissionCodeAsc(PageRequest.of(pageNo, pageSize));
        return new PagedList<>(page.getContent().stream().map(mapper::toDomain).toList(),
                page.getTotalElements());
    }
    @Override public void save(UserRoleGrant grant) { roleGrantRepository.save(mapper.toDataObject(grant)); }
    @Override public Optional<UserRoleGrant> findRoleGrantById(GrantId id) { return roleGrantRepository.findById(id.value()).map(mapper::toDomain); }
    @Override public List<UserRoleGrant> findActiveRoleGrantsByUserId(UserId id) { return roleGrantRepository.findByUserIdAndStatus(id.value(), ACTIVE).stream().map(mapper::toDomain).toList(); }
    @Override public Optional<UserRoleGrant> findActiveRoleGrantByUserIdAndRoleCodeAndSource(UserId userId, RoleCode roleCode, GrantSource source) { return roleGrantRepository.findByUserIdAndRoleCodeAndSourceTypeAndSourceIdAndStatus(userId.value(), roleCode.value(), source.sourceType().name(), source.sourceId(), ACTIVE).map(mapper::toDomain); }
    @Override public List<UserRoleGrant> findActiveRoleGrantsByUserIdAndSource(UserId userId, GrantSource source) { return roleGrantRepository.findByUserIdAndSourceTypeAndSourceIdAndStatus(userId.value(), source.sourceType().name(), source.sourceId(), ACTIVE).stream().map(mapper::toDomain).toList(); }
    @Override public void save(UserPermissionGrant grant) { permissionGrantRepository.save(mapper.toDataObject(grant)); }
    @Override public Optional<UserPermissionGrant> findActivePermissionGrantByUserIdAndPermissionCodeAndSource(UserId userId, PermissionCode permissionCode, GrantSource source) { return permissionGrantRepository.findByUserIdAndPermissionCodeAndSourceTypeAndSourceIdAndStatus(userId.value(), permissionCode.value(), source.sourceType().name(), source.sourceId(), ACTIVE).map(mapper::toDomain); }
    @Override public List<UserPermissionGrant> findActivePermissionGrantsByUserIdAndSource(UserId userId, GrantSource source) { return permissionGrantRepository.findByUserIdAndSourceTypeAndSourceIdAndStatus(userId.value(), source.sourceType().name(), source.sourceId(), ACTIVE).stream().map(mapper::toDomain).toList(); }
    @Override public void save(ChannelAuthorizationPolicy policy) {
        policyRepository.save(mapper.toDataObject(policy));
        replacePolicyDetails(policy);
    }
    @Override public Optional<ChannelAuthorizationPolicy> findByChannelCode(ChannelCode code) {
        return policyRepository.findById(code.value()).map(this::toDomain);
    }
    @Override public boolean updateIfVersionMatches(ChannelAuthorizationPolicy policy, Long expectedVersion) {
        int updated = policyRepository.updateByChannelCodeAndVersion(
                policy.getChannelCode().value(), expectedVersion, policy.getStatus().name(),
                policy.getVersion(), policy.getUpdatedAt(), policy.getActivatedAt());
        if (updated == 0) {
            return false;
        }
        replacePolicyDetails(policy);
        return true;
    }
    @Override public List<ChannelAuthorizationPolicy> findAll() {
        return policyRepository.findAll().stream().map(this::toDomain).toList();
    }
    @Override public void save(UserChannelPolicyApplication application) { applicationRepository.save(mapper.toDataObject(application)); }
    @Override public Optional<UserChannelPolicyApplication> findByUserIdAndChannelCode(UserId userId, ChannelCode channelCode) { return applicationRepository.findById(new UserChannelPolicyApplicationDO.Key(userId.value(), channelCode.value())).map(mapper::toDomain); }
    @Override public List<UserChannelPolicyApplication> findPendingApplicationsByChannelCodeAndPolicyVersion(
            ChannelCode channelCode, Long policyVersion, int batchSize
    ) {
        return applicationRepository
                .findByChannelCodeAndAppliedVersionLessThanOrderByUserIdAsc(
                        channelCode.value(), policyVersion, PageRequest.of(0, batchSize))
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<UserPermissionGrant> findPermissionGrantById(GrantId id) {
        return permissionGrantRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public List<UserPermissionGrant> findActivePermissionGrantsByUserId(UserId userId) {
        return permissionGrantRepository.findByUserIdAndStatus(userId.value(), ACTIVE).stream()
                .map(mapper::toDomain)
                .toList();
    }

    private Role toDomain(RoleDO source) {
        return Role.restore(
                new RoleCode(source.getRoleCode()), source.getRoleName(),
                com.cloud.userauth.domain.authorization.RoleStatus.valueOf(source.getStatus()),
                rolePermissionRepository.findByRoleCodeOrderByPermissionCodeAsc(source.getRoleCode()).stream()
                        .map(detail -> new PermissionCode(detail.getPermissionCode()))
                        .toList(),
                source.getCreatedAt(), source.getUpdatedAt());
    }

    private ChannelAuthorizationPolicy toDomain(ChannelAuthorizationPolicyDO source) {
        return ChannelAuthorizationPolicy.restore(
                new ChannelCode(source.getChannelCode()),
                com.cloud.userauth.domain.authorization.ChannelAuthorizationPolicyStatus
                        .valueOf(source.getStatus()),
                source.getVersion(),
                policyRoleRepository.findByChannelCodeOrderByRoleCodeAsc(source.getChannelCode()).stream()
                        .map(detail -> new RoleCode(detail.getRoleCode()))
                        .toList(),
                policyPermissionRepository
                        .findByChannelCodeOrderByPermissionCodeAsc(source.getChannelCode()).stream()
                        .map(detail -> new PermissionCode(detail.getPermissionCode()))
                        .toList(),
                source.getCreatedAt(), source.getUpdatedAt(), source.getActivatedAt());
    }

    private void replacePolicyDetails(ChannelAuthorizationPolicy policy) {
        String channelCode = policy.getChannelCode().value();
        policyRoleRepository.deleteByChannelCode(channelCode);
        policyPermissionRepository.deleteByChannelCode(channelCode);
        policyRoleRepository.saveAll(policy.getRoleCodes().stream().map(roleCode -> {
            ChannelAuthorizationPolicyRoleDO detail = new ChannelAuthorizationPolicyRoleDO();
            detail.setChannelCode(channelCode);
            detail.setRoleCode(roleCode.value());
            return detail;
        }).toList());
        policyPermissionRepository.saveAll(policy.getDirectPermissionCodes().stream().map(permissionCode -> {
            ChannelAuthorizationPolicyPermissionDO detail = new ChannelAuthorizationPolicyPermissionDO();
            detail.setChannelCode(channelCode);
            detail.setPermissionCode(permissionCode.value());
            return detail;
        }).toList());
    }
}
