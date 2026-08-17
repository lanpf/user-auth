package com.cloud.userauth.application.session.handoff;

import com.cloud.userauth.application.authentication.AuthenticatedSession;
import com.cloud.userauth.application.port.H5SessionStore;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Validated
@RequiredArgsConstructor
public class H5SessionHandoffCommandService {
    private final SessionHandoffTicketService ticketService;
    private final H5SessionStore h5SessionStore;
    private final Duration idleTtl;
    private final Duration absoluteTtl;

    public CreateH5SessionHandoffCommandOutput create(
            AuthenticatedSession authenticatedSession
    ) {
        IssuedSessionHandoff handoff = ticketService.issue(
                authenticatedSession, SessionHandoffTarget.H5_SESSION);
        return new CreateH5SessionHandoffCommandOutput(
                handoff.handoffId(), handoff.ticket());
    }

    public ExchangeH5SessionHandoffCommandOutput exchange(String ticket) {
        ConsumedSessionHandoff handoff = ticketService.consume(
                ticket, SessionHandoffTarget.H5_SESSION);
        H5SessionStore.H5Session h5Session = h5SessionStore.create(
                handoff.authenticatedSession(), idleTtl, absoluteTtl);
        return new ExchangeH5SessionHandoffCommandOutput(
                h5Session.credential(), h5Session.idleTtl(), handoff.handoffId());
    }
}
