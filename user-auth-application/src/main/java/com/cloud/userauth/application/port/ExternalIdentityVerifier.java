package com.cloud.userauth.application.port;

import com.cloud.userauth.domain.authentication.external.ExternalIdentity;
import com.cloud.userauth.domain.authentication.external.ProofType;
import java.util.Map;

public interface ExternalIdentityVerifier {
    String issuerCode();

    ExternalIdentity verify(ProofType proofType, Map<String, String> proofParameters);
}
