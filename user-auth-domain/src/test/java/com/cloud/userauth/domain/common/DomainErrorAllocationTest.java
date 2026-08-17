package com.cloud.userauth.domain.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DomainErrorAllocationTest {
    @Test
    void shouldAllocateRequiredChannelAuthorizationPolicyErrorsFirst() {
        assertEquals(500, DomainError.CHANNEL_AUTHORIZATION_POLICY_NOT_FOUND.getLocalCode());
        assertEquals(501, DomainError.CHANNEL_AUTHORIZATION_POLICY_ALREADY_EXISTS.getLocalCode());
        assertEquals(502, DomainError.CHANNEL_AUTHORIZATION_POLICY_VERSION_CONFLICT.getLocalCode());
    }
}
