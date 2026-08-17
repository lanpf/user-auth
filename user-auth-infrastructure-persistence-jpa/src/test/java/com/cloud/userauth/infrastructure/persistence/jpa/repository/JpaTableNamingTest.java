package com.cloud.userauth.infrastructure.persistence.jpa.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.cloud.framework.starter.persistence.jpa.naming.PersistencePhysicalNamingStrategy;
import com.cloud.userauth.infrastructure.persistence.jpa.model.AuthAccountDO;
import org.hibernate.boot.model.naming.Identifier;
import org.junit.jupiter.api.Test;

class JpaTableNamingTest {
    private final PersistencePhysicalNamingStrategy namingStrategy =
            new PersistencePhysicalNamingStrategy("ua_", "_d_o");

    @Test
    void shouldResolveDomainDataObjectTableName() {
        assertEquals("ua_auth_account", tableName(AuthAccountDO.class.getSimpleName()));
    }

    private String tableName(String logicalName) {
        return namingStrategy
                .toPhysicalTableName(Identifier.toIdentifier(logicalName), null)
                .getText();
    }
}
