package com.cloud.userauth.domain.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DomainErrorAllocationTest {
    @Test
    void shouldAllocateSharedDomainErrorsInRequiredOrder() {
        assertEquals(0, DomainError.DOMAIN_ENTITY_ID_INVALID.getLocalCode());
        assertEquals(1, DomainError.DOMAIN_EVENT_ID_REQUIRED.getLocalCode());
        assertEquals(2, DomainError.DOMAIN_OBJECT_FIELD_REQUIRED.getLocalCode());
        assertEquals(3, DomainError.DOMAIN_OBJECT_FIELD_INVALID.getLocalCode());
        assertEquals(4, DomainError.DOMAIN_OBJECT_STATE_INVALID.getLocalCode());
        assertEquals("领域字段不能为空", DomainError.DOMAIN_OBJECT_FIELD_REQUIRED.getMessage());
    }

    @Test
    void shouldAllocateRequiredChannelAuthorizationPolicyErrorsFirst() {
        assertEquals(500, DomainError.CHANNEL_AUTHORIZATION_POLICY_NOT_FOUND.getLocalCode());
        assertEquals(501, DomainError.CHANNEL_AUTHORIZATION_POLICY_ALREADY_EXISTS.getLocalCode());
        assertEquals(502, DomainError.CHANNEL_AUTHORIZATION_POLICY_VERSION_CONFLICT.getLocalCode());
    }
}
