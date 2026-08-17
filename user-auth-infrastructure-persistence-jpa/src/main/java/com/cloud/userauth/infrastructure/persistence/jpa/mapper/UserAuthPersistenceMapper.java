package com.cloud.userauth.infrastructure.persistence.jpa.mapper;

import com.cloud.userauth.application.registration.RegistrationProcess;
import com.cloud.userauth.domain.authentication.account.AuthAccount;
import com.cloud.userauth.domain.authentication.account.AuthAccountId;
import com.cloud.userauth.domain.authentication.challenge.AuthChallenge;
import com.cloud.userauth.domain.authentication.credential.Credential;
import com.cloud.userauth.domain.authentication.loginattempt.LoginAttempt;
import com.cloud.userauth.domain.authentication.session.LoginSession;
import com.cloud.userauth.infrastructure.persistence.jpa.model.AuthAccountDO;
import com.cloud.userauth.infrastructure.persistence.jpa.model.AuthChallengeDO;
import com.cloud.userauth.infrastructure.persistence.jpa.model.CredentialDO;
import com.cloud.userauth.infrastructure.persistence.jpa.model.LoginSessionDO;
import com.cloud.userauth.infrastructure.persistence.jpa.model.LoginAttemptDO;
import com.cloud.userauth.infrastructure.persistence.jpa.model.RegistrationProcessDO;
import java.util.List;

/** 持久化映射契约。 */
public interface UserAuthPersistenceMapper {
    AuthAccountDO toDataObject(AuthAccount source);

    CredentialDO toDataObject(AuthAccountId accountId, Credential source);

    AuthChallengeDO toDataObject(AuthChallenge source);

    RegistrationProcessDO toDataObject(RegistrationProcess source);

    LoginSessionDO toDataObject(LoginSession source);

    LoginAttemptDO toDataObject(LoginAttempt source);

    AuthAccount toDomain(AuthAccountDO source, List<CredentialDO> credentials);

    Credential toDomain(CredentialDO source);

    AuthChallenge toDomain(AuthChallengeDO source);

    RegistrationProcess toDomain(RegistrationProcessDO source);

    LoginSession toDomain(LoginSessionDO source);

    LoginAttempt toDomain(LoginAttemptDO source);

}
