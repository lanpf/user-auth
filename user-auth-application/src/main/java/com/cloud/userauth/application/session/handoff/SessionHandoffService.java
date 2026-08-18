package com.cloud.userauth.application.session.handoff;

import com.cloud.userauth.application.authentication.AuthenticatedSession;
import com.cloud.userauth.application.common.ApplicationError;
import com.cloud.userauth.application.common.ApplicationException;
import com.cloud.userauth.application.port.SessionHandoffStore;
import com.cloud.userauth.domain.authentication.session.LoginSession;
import com.cloud.userauth.domain.authentication.session.LoginSessionRepository;
import com.cloud.userauth.domain.authentication.session.SessionStatus;
import com.cloud.userauth.domain.authentication.session.SessionId;
import com.cloud.userauth.domain.user.UserId;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

/** 与目标会话实现无关的一次性交接票据签发与消费服务。 */
@RequiredArgsConstructor
public class SessionHandoffService {
    private final SessionHandoffStore handoffStore;
    private final Duration ticketTtl;
    private final LoginSessionRepository loginSessionRepository;
    private final Clock clock;

    public IssuedSessionHandoff issue(
            Long authenticatedUserId,
            String authenticatedSessionId,
            SessionHandoffTarget target
    ) {
        Instant now = clock.instant();
        LoginSession resolvedSession = loginSessionRepository.findById(new SessionId(authenticatedSessionId))
                .filter(session -> session.getUserId().equals(new UserId(authenticatedUserId)))
                .orElseThrow(() -> new ApplicationException(
                        ApplicationError.APP_SESSION_HANDOFF_TICKET_INVALID));
        AuthenticatedSession authenticatedSession = new AuthenticatedSession(
                resolvedSession.getUserId().value(),
                resolvedSession.getAuthAccountId().value(),
                resolvedSession.id());
        LoginSession loginSession = activeLoginSession(authenticatedSession, now);
        Duration effectiveTtl = effectiveTtl(loginSession, now);
        SessionHandoffStore.IssuedTicket ticket = handoffStore.issue(
                authenticatedSession, UUID.randomUUID().toString(), target, effectiveTtl);
        return new IssuedSessionHandoff(ticket.handoffId(), ticket.ticket());
    }

    public ConsumedSessionHandoff consume(
            String ticket,
            SessionHandoffTarget expectedTarget
    ) {
        SessionHandoffStore.ConsumedTicket consumed = handoffStore.consume(ticket)
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
