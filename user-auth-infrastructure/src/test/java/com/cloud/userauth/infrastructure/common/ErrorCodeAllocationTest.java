package com.cloud.userauth.infrastructure.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cloud.framework.core.error.BaseError;
import com.cloud.userauth.application.common.ApplicationError;
import com.cloud.userauth.domain.common.DomainError;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class ErrorCodeAllocationTest {
    @Test
    void shouldAllocateDomainErrorsToSharedAndAggregateBlocks() {
        assertEquals(0, DomainError.DOMAIN_ENTITY_ID_INVALID.getLocalCode());
        assertEquals(2, DomainError.DOMAIN_OBJECT_FIELD_REQUIRED.getLocalCode());
        assertRange(DomainError.AUTH_ACCOUNT_NOT_FOUND.getLocalCode(), 100, 149);
        assertRange(DomainError.AUTH_CHALLENGE_NOT_FOUND.getLocalCode(), 150, 199);
        assertRange(DomainError.LOGIN_ATTEMPT_NOT_FOUND.getLocalCode(), 200, 249);
        assertRange(DomainError.LOGIN_SESSION_NOT_FOUND.getLocalCode(), 250, 299);
        assertRange(DomainError.ROLE_NOT_FOUND.getLocalCode(), 300, 349);
        assertRange(DomainError.USER_ROLE_GRANT_NOT_FOUND.getLocalCode(), 350, 399);
        assertRange(DomainError.PERMISSION_NOT_FOUND.getLocalCode(), 400, 449);
        assertRange(DomainError.USER_PERMISSION_GRANT_NOT_FOUND.getLocalCode(), 450, 499);
        assertRange(DomainError.CHANNEL_AUTHORIZATION_POLICY_NOT_FOUND.getLocalCode(), 500, 549);
    }

    @Test
    void shouldAllocateApplicationErrorsToApplicationRange() {
        for (ApplicationError error : ApplicationError.values()) {
            assertRange(error.getLocalCode(), 600, 699);
            assertTrue(error.name().startsWith("APP_"));
        }
    }

    @Test
    void shouldAllocateInfrastructureErrorsByTechnicalAndAclRanges() {
        assertRange(
                InfrastructureError.INFRA_TECH_LOCK_EXECUTION_FAILED.getLocalCode(),
                700,
                799);
        for (InfrastructureError error : InfrastructureError.values()) {
            if (error.name().startsWith("INFRA_TECH_")) {
                assertRange(error.getLocalCode(), 700, 799);
            } else {
                assertTrue(error.name().startsWith("INFRA_ACL_"));
                assertRange(error.getLocalCode(), 800, 899);
            }
        }
    }

    @Test
    void shouldKeepCompleteErrorCodesUniqueWithinUserAuth() {
        List<BaseError> errors = Stream.of(
                        Arrays.stream(DomainError.values()),
                        Arrays.stream(ApplicationError.values()),
                        Arrays.stream(InfrastructureError.values()))
                .flatMap(stream -> stream)
                .map(BaseError.class::cast)
                .toList();
        Set<String> completeCodes = errors.stream()
                .map(BaseError::getErrorCode)
                .collect(java.util.stream.Collectors.toSet());

        assertEquals(errors.size(), completeCodes.size());
        assertEquals("200000", DomainError.DOMAIN_ENTITY_ID_INVALID.getErrorCode());
        assertTrue(errors.stream().allMatch(error -> error.getErrorCode().startsWith("200")));
    }

    private static void assertRange(int localCode, int lowerBound, int upperBound) {
        assertTrue(
                localCode >= lowerBound && localCode <= upperBound,
                () -> "Expected local code %s to be in range %s-%s"
                        .formatted(localCode, lowerBound, upperBound));
    }
}
