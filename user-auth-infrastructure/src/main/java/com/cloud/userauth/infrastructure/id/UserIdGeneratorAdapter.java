package com.cloud.userauth.infrastructure.id;

import com.cloud.framework.id.LongIdGenerator;
import com.cloud.userauth.domain.user.UserId;
import com.cloud.userauth.domain.user.UserIdGenerator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserIdGeneratorAdapter implements UserIdGenerator {
    private final LongIdGenerator idGenerator;
    @Override public UserId nextId() { return new UserId(idGenerator.nextId(IdGeneratorNames.USER)); }
}
