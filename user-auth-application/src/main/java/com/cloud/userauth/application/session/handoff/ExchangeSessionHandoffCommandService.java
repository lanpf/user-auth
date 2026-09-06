package com.cloud.userauth.application.session.handoff;

import com.cloud.userauth.application.port.BrowserSessionStore;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Validated
@RequiredArgsConstructor
public class ExchangeSessionHandoffCommandService {
    private final SessionHandoffService handoffService;
    private final BrowserSessionStore browserSessionStore;
    private final Duration idleTtl;
    private final Duration absoluteTtl;

    public ExchangeSessionHandoffOutput execute(ExchangeSessionHandoffCommand command) {
        ConsumedSessionHandoff handoff = handoffService.consume(command.ticket(), command.target());
        BrowserSessionStore.CreatedSession session = browserSessionStore.create(
                handoff.authenticatedSession(), idleTtl, absoluteTtl);
        return new ExchangeSessionHandoffOutput(
                session.credential(), session.absoluteTtl(), handoff.handoffId());
    }
}
