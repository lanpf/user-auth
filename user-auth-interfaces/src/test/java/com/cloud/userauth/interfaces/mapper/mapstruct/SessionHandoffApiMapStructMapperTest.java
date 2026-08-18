package com.cloud.userauth.interfaces.mapper.mapstruct;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.cloud.userauth.api.authentication.CreateSessionHandoffApiCommand;
import com.cloud.userauth.api.enums.SessionHandoffTargetApiEnum;
import com.cloud.userauth.application.session.handoff.CreateSessionHandoffCommand;
import com.cloud.userauth.application.session.handoff.SessionHandoffTarget;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class SessionHandoffApiMapStructMapperTest {
    private final SessionHandoffApiMapStructMapper mapper =
            Mappers.getMapper(SessionHandoffApiMapStructMapper.class);

    @Test
    void shouldMapAuthenticatedSessionIdentity() {
        CreateSessionHandoffCommand command = mapper.toCommand(
                new CreateSessionHandoffApiCommand(
                        1001L,
                        "session-1",
                        SessionHandoffTargetApiEnum.BROWSER_SESSION));

        assertEquals(1001L, command.authenticatedUserId());
        assertEquals("session-1", command.authenticatedSessionId());
        assertEquals(SessionHandoffTarget.BROWSER_SESSION, command.target());
    }
}
