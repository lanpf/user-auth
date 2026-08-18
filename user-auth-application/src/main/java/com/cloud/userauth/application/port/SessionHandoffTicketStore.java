package com.cloud.userauth.application.port;

import com.cloud.userauth.application.authentication.AuthenticatedSession;
import com.cloud.userauth.application.session.handoff.SessionHandoffTarget;
import java.time.Duration;
import java.util.Optional;

/** 会话一次性交接票据的存储端口。 */
public interface SessionHandoffTicketStore extends LoginSessionRevoker {
    IssuedTicket issue(
            AuthenticatedSession authenticatedSession,
            String handoffId,
            SessionHandoffTarget target,
            Duration ttl
    );

    Optional<ConsumedTicket> consume(String ticket);

    record IssuedTicket(String handoffId, String ticket) {
    }

    record ConsumedTicket(
            AuthenticatedSession authenticatedSession,
            String handoffId,
            SessionHandoffTarget target
    ) {
    }
}
