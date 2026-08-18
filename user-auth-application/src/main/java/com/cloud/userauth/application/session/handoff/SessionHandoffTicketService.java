package com.cloud.userauth.application.session.handoff;

import com.cloud.userauth.application.authentication.AuthenticatedSession;
import com.cloud.userauth.application.common.ApplicationError;
import com.cloud.userauth.application.common.ApplicationException;
import com.cloud.userauth.application.port.SessionHandoffTicketStore;
import com.cloud.userauth.domain.authentication.session.LoginSession;
import com.cloud.userauth.domain.authentication.session.LoginSessionRepository;
import com.cloud.userauth.domain.authentication.session.SessionStatus;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

/** 与目标会话实现无关的一次性交接票据签发与消费服务。 */
@RequiredArgsConstructor
public class SessionHandoffTicketService {
    private final SessionHandoffTicketStore ticketStore;
    private final Duration ticketTtl;
    private final LoginSessionRepository loginSessionRepository;
    private final Clock clock;

    public IssuedSessionHandoff issue(
            AuthenticatedSession authenticatedSession,
            SessionHandoffTarget target
    ) {
        Instant now = clock.instant();
        LoginSession loginSession = activeLoginSession(authenticatedSession, now);
        Duration effectiveTtl = effectiveTtl(loginSession, now);
        SessionHandoffTicketStore.IssuedTicket ticket = ticketStore.issue(
                authenticatedSession, UUID.randomUUID().toString(), target, effectiveTtl);
        return new IssuedSessionHandoff(ticket.handoffId(), ticket.ticket());
    }

    public ConsumedSessionHandoff consume(
            String ticket,
            SessionHandoffTarget expectedTarget
    ) {
        SessionHandoffTicketStore.ConsumedTicket consumed = ticketStore.consume(ticket)
                .filter(value -> value.target() == expectedTarget)
                .orElseThrow(() -> new ApplicationException(
                        ApplicationError.APP_SESSION_HANDOFF_TICKET_INVALID));
        activeLoginSession(consumed.authenticatedSession(), clock.instant());
        return new ConsumedSessionHandoff(
                consumed.authenticatedSession(), consumed.handoffId());
    }

    private LoginSession activeLoginSession(
            AuthenticatedSession authenticatedSession,
            Instant now
    ) {
        return loginSessionRepository.findById(authenticatedSession.sessionId())
                .filter(session -> session.getStatus() == SessionStatus.ACTIVE)
                .filter(session -> session.getExpiresAt().isAfter(now))
                .filter(session -> session.getUserId().value().equals(authenticatedSession.userId()))
                .filter(session -> session.getAuthAccountId().value()
                        .equals(authenticatedSession.authAccountId()))
                .orElseThrow(() -> new ApplicationException(
                        ApplicationError.APP_SESSION_HANDOFF_TICKET_INVALID));
    }

    private Duration effectiveTtl(LoginSession loginSession, Instant now) {
        Duration remainingLoginSessionTtl = Duration.between(now, loginSession.getExpiresAt());
        return ticketTtl.compareTo(remainingLoginSessionTtl) < 0
                ? ticketTtl
                : remainingLoginSessionTtl;
    }
}
