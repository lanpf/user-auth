package com.cloud.userauth.domain.authorization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cloud.userauth.domain.common.DomainError;
import com.cloud.userauth.domain.common.DomainException;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class ChannelAuthorizationPolicyTest {
    private static final Instant CREATED_AT = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void shouldDeduplicateTargetsAndAdvanceVersionOnActivation() {
        RoleCode role = new RoleCode("buyer");
        PermissionCode permission = new PermissionCode("order:refund");
        ChannelAuthorizationPolicy policy = ChannelAuthorizationPolicy.draft(
                new ChannelCode("PARTNER_A"),
                List.of(role, role),
                List.of(permission, permission),
                CREATED_AT);

        policy.activate(1L, CREATED_AT.plusSeconds(1));

        assertTrue(policy.isActive());
        assertEquals(2L, policy.getVersion());
        assertEquals(List.of(role), policy.getRoleCodes());
        assertEquals(List.of(permission), policy.getDirectPermissionCodes());
    }

    @Test
    void shouldRejectAStaleExpectedVersion() {
        ChannelAuthorizationPolicy policy = ChannelAuthorizationPolicy.draft(
                new ChannelCode("PARTNER_A"), List.of(), List.of(), CREATED_AT);

        DomainException exception = assertThrows(
                DomainException.class,
                () -> policy.activate(2L, CREATED_AT.plusSeconds(1)));

        assertEquals(
                DomainError.CHANNEL_AUTHORIZATION_POLICY_VERSION_CONFLICT.errorCode(),
                exception.getErrorCode());
    }
}
