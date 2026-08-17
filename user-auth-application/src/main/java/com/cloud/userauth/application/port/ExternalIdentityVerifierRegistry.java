package com.cloud.userauth.application.port;

import com.cloud.userauth.domain.authentication.external.ExternalIdentity;
import com.cloud.userauth.domain.authentication.external.ProofType;
import java.util.Map;

public interface ExternalIdentityVerifierRegistry {
    ExternalIdentity verify(
            String issuerCode,
            ProofType proofType,
            Map<String, String> proofParameters
    );
}
