package com.cloud.userauth.application.authorization;

import com.cloud.userauth.domain.authorization.ChannelAuthorizationPolicy;
import com.cloud.userauth.domain.authorization.ChannelAuthorizationPolicyRepository;
import com.cloud.userauth.domain.authorization.ChannelCode;
import com.cloud.userauth.domain.authorization.PermissionCode;
import com.cloud.userauth.domain.authorization.RoleCode;
import com.cloud.userauth.domain.authorization.service.AuthorizationDomainService;
import com.cloud.userauth.domain.common.DomainError;
import com.cloud.userauth.domain.common.DomainException;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class ChannelAuthorizationPolicyCommandService {
    private final ChannelAuthorizationPolicyRepository policyRepository;
    private final AuthorizationDomainService authorizationDomainService;
    private final ChannelAuthorizationPolicySynchronizationService synchronizationService;
    private final Clock clock;

    @Transactional
    public ChannelAuthorizationPolicyView save(SaveChannelAuthorizationPolicyCommand command) {
        ChannelCode channelCode = new ChannelCode(command.channelCode());
        List<RoleCode> roleCodes = command.roleCodes().stream().map(RoleCode::new).toList();
        List<PermissionCode> permissionCodes = command.directPermissionCodes().stream()
                .map(PermissionCode::new)
                .toList();
        authorizationDomainService.validatePolicyTargets(roleCodes, permissionCodes);
        Instant now = clock.instant();
        ChannelAuthorizationPolicy policy = policyRepository.findByChannelCode(channelCode)
                .orElse(null);
        if (policy == null) {
            if (command.expectedVersion() != null) {
                throw new DomainException(
                        DomainError.CHANNEL_AUTHORIZATION_POLICY_VERSION_CONFLICT);
            }
            policy = ChannelAuthorizationPolicy.draft(
                    channelCode, roleCodes, permissionCodes, now);
        } else {
            policy.replaceTargets(
                    command.expectedVersion(), roleCodes, permissionCodes, now);
        }
        saveExistingOrCreate(policy, command.expectedVersion());
        return view(policy);
    }

    @Transactional
    public ChannelAuthorizationPolicyView activate(
            ChangeChannelAuthorizationPolicyStatusCommand command
    ) {
        ChannelAuthorizationPolicy policy = requiredForUpdate(command.channelCode());
        authorizationDomainService.validatePolicyTargets(
                policy.getRoleCodes(), policy.getDirectPermissionCodes());
        policy.activate(command.expectedVersion(), clock.instant());
        saveUpdated(policy, command.expectedVersion());
        return view(policy);
    }

    @Transactional
    public ChannelAuthorizationPolicyView disable(
            ChangeChannelAuthorizationPolicyStatusCommand command
    ) {
        ChannelAuthorizationPolicy policy = requiredForUpdate(command.channelCode());
        policy.disable(command.expectedVersion(), clock.instant());
        saveUpdated(policy, command.expectedVersion());
        return view(policy);
    }

    public ReconcileChannelAuthorizationPolicyOutput reconcile(
            ReconcileChannelAuthorizationPolicyCommand command
    ) {
        return synchronizationService.synchronizePendingUsers(command);
    }

    private ChannelAuthorizationPolicy required(String channelCode) {
        return policyRepository.findByChannelCode(new ChannelCode(channelCode))
                .orElseThrow(() -> new DomainException(
                        DomainError.CHANNEL_AUTHORIZATION_POLICY_NOT_FOUND));
    }

    private ChannelAuthorizationPolicy requiredForUpdate(String channelCode) {
        return policyRepository.findByChannelCode(new ChannelCode(channelCode))
                .orElseThrow(() -> new DomainException(
                        DomainError.CHANNEL_AUTHORIZATION_POLICY_NOT_FOUND));
    }

    private void saveExistingOrCreate(ChannelAuthorizationPolicy policy, Long expectedVersion) {
        if (expectedVersion == null) {
            policyRepository.save(policy);
            return;
        }
        saveUpdated(policy, expectedVersion);
    }

    private void saveUpdated(ChannelAuthorizationPolicy policy, Long expectedVersion) {
        if (!policyRepository.updateIfVersionMatches(policy, expectedVersion)) {
            throw new DomainException(DomainError.CHANNEL_AUTHORIZATION_POLICY_VERSION_CONFLICT);
        }
    }

    static ChannelAuthorizationPolicyView view(ChannelAuthorizationPolicy policy) {
        return new ChannelAuthorizationPolicyView(
                policy.getChannelCode().value(),
                policy.getStatus().name(),
                policy.getVersion(),
                policy.getRoleCodes().stream().map(RoleCode::value).toList(),
                policy.getDirectPermissionCodes().stream().map(PermissionCode::value).toList(),
                policy.getCreatedAt(),
                policy.getUpdatedAt(),
                policy.getActivatedAt());
    }
}
