package com.cloud.userauth.application.login.external;

import com.cloud.userauth.domain.authentication.account.AuthAccountId;
import com.cloud.userauth.domain.authentication.credential.CredentialId;
import com.cloud.userauth.domain.user.UserId;

public record ExternalAccountPreparationOutput(
        UserId userId,
        AuthAccountId authAccountId,
        CredentialId externalCredentialId
) {
}
