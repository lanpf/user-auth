package com.cloud.userauth.domain.authentication.loginattempt;

import com.cloud.framework.domain.Repository;
import com.cloud.userauth.domain.authentication.credential.CredentialIssuer;
import com.cloud.userauth.domain.authentication.credential.Principal;
import java.util.Optional;

public interface LoginAttemptRepository extends Repository<LoginAttempt, LoginAttemptId> {
    Optional<LoginAttempt> findPendingByIssuerAndExternalPrincipal(
            CredentialIssuer issuer,
            Principal externalPrincipal
    );
}
