package com.cloud.userauth.infrastructure.external;

import com.cloud.userauth.application.common.ApplicationError;
import com.cloud.userauth.application.common.ApplicationException;
import com.cloud.userauth.application.port.ExternalIdentityVerifier;
import com.cloud.userauth.application.port.ExternalIdentityVerifierRegistry;
import com.cloud.userauth.domain.authentication.external.ExternalIdentity;
import com.cloud.userauth.domain.authentication.external.ProofType;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultExternalIdentityVerifierRegistry implements ExternalIdentityVerifierRegistry {
    private final List<ExternalIdentityVerifier> verifiers;

    @Override
    public ExternalIdentity verify(
            String issuerCode,
            ProofType proofType,
            Map<String, String> proofParameters
    ) {
        ExternalIdentityVerifier verifier = verifiers.stream()
                .filter(candidate -> candidate.issuerCode().equalsIgnoreCase(issuerCode))
                .findFirst()
                .orElseThrow(() -> new ApplicationException(
                        ApplicationError.APP_EXTERNAL_IDENTITY_ISSUER_NOT_SUPPORTED));
        ExternalIdentity identity = verifier.verify(proofType, proofParameters);
        if (identity == null || !identity.issuer().code().equalsIgnoreCase(issuerCode)) {
            throw new ApplicationException(ApplicationError.APP_LOGIN_REJECTED);
        }
        return identity;
    }
}
