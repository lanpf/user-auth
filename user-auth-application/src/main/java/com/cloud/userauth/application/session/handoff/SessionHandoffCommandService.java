package com.cloud.userauth.application.session.handoff;

import com.cloud.userauth.application.port.BrowserSessionStore;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Validated
@RequiredArgsConstructor
public class SessionHandoffCommandService {
    private final SessionHandoffService handoffService;
    private final BrowserSessionStore browserSessionStore;
    private final Duration idleTtl;
    private final Duration absoluteTtl;

    public CreateSessionHandoffOutput create(CreateSessionHandoffCommand command) {
        IssuedSessionHandoff handoff = handoffService.issue(
                command.authenticatedUserId(), command.authenticatedSessionId(), command.target());
        return new CreateSessionHandoffOutput(
                handoff.handoffId(), handoff.ticket());
    }

    public ExchangeSessionHandoffOutput exchange(ExchangeSessionHandoffCommand command) {
        ConsumedSessionHandoff handoff = handoffService.consume(command.ticket(), command.target());
        BrowserSessionStore.CreatedSession session = browserSessionStore.create(
                handoff.authenticatedSession(), idleTtl, absoluteTtl);
        return new ExchangeSessionHandoffOutput(
                session.credential(), session.idleTtl(), handoff.handoffId());
    }
}
