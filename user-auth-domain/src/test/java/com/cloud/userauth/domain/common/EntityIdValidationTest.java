package com.cloud.userauth.domain.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cloud.userauth.domain.authentication.session.SessionId;
import com.cloud.userauth.domain.authorization.ChannelCode;
import com.cloud.userauth.domain.authorization.PermissionCode;
import com.cloud.userauth.domain.authorization.RoleCode;
import com.cloud.userauth.domain.user.UserId;
import org.junit.jupiter.api.Test;

class EntityIdValidationTest {

    @Test
    void shouldValidateAndCompareLongEntityIds() {
        UserId userId = new UserId(1L);

        assertEquals(1L, userId.value());
        assertEquals(new UserId(1L), userId);
        assertEquals(new UserId(1L).hashCode(), userId.hashCode());
        assertNotEquals(new UserId(2L), userId);
        assertInvalidEntityId(() -> new UserId(null));
        assertInvalidEntityId(() -> new UserId(0L));
        assertInvalidEntityId(() -> new UserId(-1L));
    }

    @Test
    void shouldTrimAndValidateStringEntityIds() {
        SessionId sessionId = new SessionId(" session-1 ");

        assertEquals("session-1", sessionId.value());
        assertEquals(new SessionId("session-1"), sessionId);
        assertInvalidEntityId(() -> new SessionId(null));
        assertInvalidEntityId(() -> new SessionId("   "));
    }

    @Test
    void shouldApplyAdditionalConcreteIdentifierConstraints() {
        assertEquals("PARTNER_A", new ChannelCode(" PARTNER_A ").value());
        assertEquals("buyer", new RoleCode(" buyer ").value());
        assertEquals("order:refund", new PermissionCode(" order:refund ").value());

        assertDomainError(
                DomainError.DOMAIN_ENTITY_ID_INVALID,
                () -> new ChannelCode("invalid channel"));
        assertDomainError(DomainError.ROLE_CODE_INVALID, () -> new RoleCode("Buyer"));
        assertDomainError(
                DomainError.PERMISSION_CODE_INVALID,
                () -> new PermissionCode("order"));
    }

    private static void assertInvalidEntityId(Runnable action) {
        assertDomainError(DomainError.DOMAIN_ENTITY_ID_INVALID, action);
    }

    private static void assertDomainError(DomainError expected, Runnable action) {
        DomainException exception = assertThrows(DomainException.class, action::run);
        assertEquals(expected.getErrorCode(), exception.getErrorCode());
    }
}
