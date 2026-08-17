-- Keep table prefix/suffix synchronized with framework.persistence.naming at runtime.
CREATE TABLE IF NOT EXISTS ua_auth_account (
    id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_ua_auth_account_user (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ua_credential (
    id BIGINT NOT NULL,
    auth_account_id BIGINT NOT NULL,
    credential_type VARCHAR(32) NOT NULL,
    issuer VARCHAR(64) NOT NULL,
    issuer_type VARCHAR(64) NOT NULL,
    principal VARCHAR(256) NOT NULL,
    status VARCHAR(32) NOT NULL,
    verified_at DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    active_credential_key VARCHAR(384)
        GENERATED ALWAYS AS (
            CASE WHEN status = 'ACTIVE'
                THEN CONCAT(credential_type, ':', issuer, ':', principal)
                ELSE NULL
            END
        ) STORED,
    active_mobile_account BIGINT
        GENERATED ALWAYS AS (
            CASE WHEN status = 'ACTIVE' AND credential_type = 'MOBILE'
                THEN auth_account_id
                ELSE NULL
            END
        ) STORED,
    PRIMARY KEY (id),
    UNIQUE KEY uk_ua_active_credential (active_credential_key),
    UNIQUE KEY uk_ua_active_mobile_account (active_mobile_account),
    KEY idx_ua_credential_account (auth_account_id),
    CONSTRAINT fk_ua_credential_account FOREIGN KEY (auth_account_id) REFERENCES ua_auth_account (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ua_auth_challenge (
    id BIGINT NOT NULL,
    challenge_type VARCHAR(32) NOT NULL,
    target VARCHAR(256) NOT NULL,
    scene VARCHAR(64) NOT NULL,
    secret_hash VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    reusable_until DATETIME(6) NOT NULL,
    attempts INT NOT NULL,
    verified_at DATETIME(6),
    consumed_by_type VARCHAR(64),
    consumed_by_id VARCHAR(128),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_ua_challenge_reuse (challenge_type, target, scene, status, reusable_until)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ua_registration_process (
    id BIGINT NOT NULL,
    challenge_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    auth_account_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL,
    last_failure VARCHAR(1024),
    retry_count INT NOT NULL DEFAULT 0,
    last_failed_at DATETIME(6),
    session_id VARCHAR(64),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_ua_registration_challenge (challenge_id),
    CONSTRAINT fk_ua_registration_challenge FOREIGN KEY (challenge_id) REFERENCES ua_auth_challenge (id),
    CONSTRAINT fk_ua_registration_account FOREIGN KEY (auth_account_id) REFERENCES ua_auth_account (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ua_login_session (
    id VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    auth_account_id BIGINT NOT NULL,
    authenticated_credential_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL,
    login_scene VARCHAR(32) NOT NULL,
    device_id VARCHAR(128),
    device_type VARCHAR(64),
    device_name VARCHAR(128),
    client_app_id VARCHAR(128),
    client_platform VARCHAR(64),
    client_version VARCHAR(64),
    issued_at DATETIME(6) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    last_active_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_ua_session_user_status (user_id, status),
    KEY idx_ua_session_credential (authenticated_credential_id),
    CONSTRAINT fk_ua_session_account FOREIGN KEY (auth_account_id) REFERENCES ua_auth_account (id),
    CONSTRAINT fk_ua_session_credential FOREIGN KEY (authenticated_credential_id) REFERENCES ua_credential (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ua_login_attempt (
    id VARCHAR(64) NOT NULL,
    issuer VARCHAR(64) NOT NULL,
    issuer_type VARCHAR(64) NOT NULL,
    external_principal VARCHAR(256) NOT NULL,
    display_name VARCHAR(256),
    mobile VARCHAR(32),
    mobile_verified BOOLEAN NOT NULL,
    status VARCHAR(32) NOT NULL,
    session_id VARCHAR(64),
    expires_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_ua_login_attempt_identity (issuer, external_principal, status, created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ua_domain_event (
    event_id BIGINT NOT NULL,
    event_type VARCHAR(128) NOT NULL,
    occurred_at DATETIME(6) NOT NULL,
    payload JSON NOT NULL,
    PRIMARY KEY (event_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ua_permission (
    permission_code VARCHAR(128) NOT NULL, permission_name VARCHAR(128) NOT NULL,
    owner_service VARCHAR(128) NOT NULL, status VARCHAR(32) NOT NULL,
    created_at DATETIME(6) NOT NULL, updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (permission_code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ua_role (
    role_code VARCHAR(128) NOT NULL, role_name VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL, created_at DATETIME(6) NOT NULL, updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (role_code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ua_role_permission (
    role_code VARCHAR(128) NOT NULL, permission_code VARCHAR(128) NOT NULL,
    PRIMARY KEY (role_code, permission_code),
    CONSTRAINT fk_ua_role_permission_role FOREIGN KEY (role_code) REFERENCES ua_role (role_code),
    CONSTRAINT fk_ua_role_permission_permission FOREIGN KEY (permission_code) REFERENCES ua_permission (permission_code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ua_user_role_grant (
    id BIGINT NOT NULL, user_id BIGINT NOT NULL, role_code VARCHAR(128) NOT NULL,
    source_type VARCHAR(64) NOT NULL, source_id VARCHAR(128) NOT NULL, status VARCHAR(32) NOT NULL,
    granted_by BIGINT, granted_at DATETIME(6) NOT NULL, expires_at DATETIME(6), updated_at DATETIME(6) NOT NULL,
    active_grant_key VARCHAR(512) GENERATED ALWAYS AS (
        CASE WHEN status = 'ACTIVE'
            THEN CONCAT(user_id, ':', role_code, ':', source_type, ':', source_id)
            ELSE NULL
        END
    ) STORED,
    PRIMARY KEY (id),
    UNIQUE KEY uk_ua_active_user_role_grant (active_grant_key),
    KEY idx_ua_user_role_grant_user (user_id, status),
    KEY idx_ua_user_role_grant_source (source_type, source_id, status),
    CONSTRAINT fk_ua_user_role_grant_role FOREIGN KEY (role_code) REFERENCES ua_role (role_code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ua_user_permission_grant (
    id BIGINT NOT NULL, user_id BIGINT NOT NULL, permission_code VARCHAR(128) NOT NULL, status VARCHAR(32) NOT NULL,
    source_type VARCHAR(64) NOT NULL, source_id VARCHAR(128) NOT NULL,
    granted_by BIGINT, granted_at DATETIME(6) NOT NULL, expires_at DATETIME(6), reason VARCHAR(512), updated_at DATETIME(6) NOT NULL,
    active_grant_key VARCHAR(512) GENERATED ALWAYS AS (
        CASE WHEN status = 'ACTIVE'
            THEN CONCAT(user_id, ':', permission_code, ':', source_type, ':', source_id)
            ELSE NULL
        END
    ) STORED,
    PRIMARY KEY (id),
    UNIQUE KEY uk_ua_active_user_permission_grant (active_grant_key),
    KEY idx_ua_user_permission_grant_user (user_id, status),
    KEY idx_ua_user_permission_grant_source (source_type, source_id, status),
    CONSTRAINT fk_ua_user_permission_grant_permission FOREIGN KEY (permission_code) REFERENCES ua_permission (permission_code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ua_channel_authorization_policy (
    channel_code VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL,
    version BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    activated_at DATETIME(6),
    PRIMARY KEY (channel_code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ua_channel_authorization_policy_role (
    channel_code VARCHAR(128) NOT NULL,
    role_code VARCHAR(128) NOT NULL,
    PRIMARY KEY (channel_code, role_code),
    CONSTRAINT fk_ua_channel_policy_role_policy FOREIGN KEY (channel_code)
        REFERENCES ua_channel_authorization_policy (channel_code),
    CONSTRAINT fk_ua_channel_policy_role_role FOREIGN KEY (role_code)
        REFERENCES ua_role (role_code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ua_channel_authorization_policy_permission (
    channel_code VARCHAR(128) NOT NULL,
    permission_code VARCHAR(128) NOT NULL,
    PRIMARY KEY (channel_code, permission_code),
    CONSTRAINT fk_ua_channel_policy_permission_policy FOREIGN KEY (channel_code)
        REFERENCES ua_channel_authorization_policy (channel_code),
    CONSTRAINT fk_ua_channel_policy_permission_permission FOREIGN KEY (permission_code)
        REFERENCES ua_permission (permission_code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ua_user_channel_policy_application (
    user_id BIGINT NOT NULL,
    channel_code VARCHAR(128) NOT NULL,
    applied_version BIGINT NOT NULL,
    first_applied_at DATETIME(6) NOT NULL,
    last_applied_at DATETIME(6) NOT NULL,
    PRIMARY KEY (user_id, channel_code),
    KEY idx_ua_user_channel_policy_pending (channel_code, applied_version, user_id),
    CONSTRAINT fk_ua_user_channel_policy_policy FOREIGN KEY (channel_code)
        REFERENCES ua_channel_authorization_policy (channel_code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS oauth2_registered_client (
    id VARCHAR(100) NOT NULL,
    client_id VARCHAR(100) NOT NULL,
    client_id_issued_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL,
    client_secret VARCHAR(200),
    client_secret_expires_at TIMESTAMP(6),
    client_name VARCHAR(200) NOT NULL,
    client_authentication_methods VARCHAR(1000) NOT NULL,
    authorization_grant_types VARCHAR(1000) NOT NULL,
    redirect_uris VARCHAR(1000),
    post_logout_redirect_uris VARCHAR(1000),
    scopes VARCHAR(1000) NOT NULL,
    client_settings VARCHAR(2000) NOT NULL,
    token_settings VARCHAR(2000) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_oauth2_registered_client_id (client_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
