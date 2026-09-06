package com.cloud.userauth.application.session.handoff;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Validated
@RequiredArgsConstructor
public class CreateSessionHandoffCommandService {
    private final SessionHandoffService handoffService;

    public CreateSessionHandoffOutput execute(CreateSessionHandoffCommand command) {
        IssuedSessionHandoff handoff = handoffService.issue(
                command.authenticatedUserId(), command.authenticatedSessionId(), command.target());
        return new CreateSessionHandoffOutput(handoff.handoffId(), handoff.ticket());
    }
}
