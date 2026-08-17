package com.cloud.userauth.infrastructure.persistence.jpa.repository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class JpaSchemaTest {

    @Test
    void shouldDeclareCurrentRelationalPersistenceModelInSingleSchema() throws IOException {
        String schema = schema();
        assertTrue(schema.startsWith("-- Keep table prefix/suffix"));
        String registrationProcess = tableDefinition(schema, "ua_registration_process");
        String loginSession = tableDefinition(schema, "ua_login_session");

        assertFalse(registrationProcess.contains("authenticated_credential_id"));
        assertTrue(loginSession.contains("authenticated_credential_id BIGINT NOT NULL"));
        assertTrue(loginSession.contains("fk_ua_session_credential"));
        assertTrue(schema.contains("CREATE TABLE IF NOT EXISTS oauth2_registered_client"));
        assertFalse(schema.contains("CREATE TABLE IF NOT EXISTS ua_oauth2authorization"));
        assertFalse(schema.contains("CREATE TABLE IF NOT EXISTS ua_oauth2refresh_token_history"));
        assertTrue(schema.contains("CREATE TABLE IF NOT EXISTS ua_channel_authorization_policy"));
        assertTrue(schema.contains("CREATE TABLE IF NOT EXISTS ua_role_permission"));
        assertTrue(schema.contains("CREATE TABLE IF NOT EXISTS ua_channel_authorization_policy_role"));
        assertTrue(schema.contains("CREATE TABLE IF NOT EXISTS ua_channel_authorization_policy_permission"));
        assertTrue(schema.contains("CREATE TABLE IF NOT EXISTS ua_user_channel_policy_application"));
        assertTrue(schema.contains("source_type VARCHAR(64) NOT NULL"));
        assertTrue(schema.contains("uk_ua_active_user_role_grant"));
        assertTrue(schema.contains("uk_ua_active_user_permission_grant"));
    }

    private static String schema() throws IOException {
        Path schema = Path.of("..", "config", "db", "schema.sql");
        if (!Files.isRegularFile(schema)) {
            throw new IllegalStateException("external config/db/schema.sql not found");
        }
        return Files.readString(schema);
    }

    private static String tableDefinition(String schema, String tableName) {
        String marker = "CREATE TABLE IF NOT EXISTS " + tableName;
        int start = schema.indexOf(marker);
        if (start < 0) {
            throw new IllegalStateException(tableName + " not found in db/schema.sql");
        }
        int end = schema.indexOf(';', start);
        if (end < 0) {
            throw new IllegalStateException(tableName + " definition is incomplete");
        }
        return schema.substring(start, end);
    }
}
