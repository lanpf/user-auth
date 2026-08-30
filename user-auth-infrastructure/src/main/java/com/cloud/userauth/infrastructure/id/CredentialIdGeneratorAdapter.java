package com.cloud.userauth.infrastructure.id;

import com.cloud.framework.id.LongIdGenerator;
import com.cloud.userauth.domain.authentication.credential.CredentialId;
import com.cloud.userauth.domain.authentication.credential.CredentialIdGenerator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CredentialIdGeneratorAdapter implements CredentialIdGenerator {
    private final LongIdGenerator idGenerator;
    @Override public CredentialId nextId() { return new CredentialId(idGenerator.nextId(IdGeneratorScene.CREDENTIAL.getValue())); }
}
