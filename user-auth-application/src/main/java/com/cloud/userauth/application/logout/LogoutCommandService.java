package com.cloud.userauth.application.logout;

import com.cloud.framework.domain.DomainEventStore;
import com.cloud.userauth.application.port.LoginSessionRevoker;
import com.cloud.userauth.domain.common.DomainError;
import com.cloud.userauth.domain.common.DomainException;
import com.cloud.userauth.domain.authentication.service.SessionDomainService;
import com.cloud.userauth.domain.authentication.service.SessionRevocationEffect;
import com.cloud.userauth.domain.authentication.session.LoginSession;
import com.cloud.userauth.domain.authentication.session.LoginSessionRepository;
import com.cloud.userauth.domain.authentication.session.SessionId;
import com.cloud.userauth.domain.user.UserId;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Validated
@RequiredArgsConstructor
public class LogoutCommandService {
    private final LoginSessionRepository sessionRepository;
    private final List<LoginSessionRevoker> loginSessionRevokers;
    private final SessionDomainService sessionDomainService;
    private final DomainEventStore domainEventStore;
    private final Clock clock;

    @Transactional
    public LogoutOutput execute(LogoutCommand command) {
        SessionId sessionId = new SessionId(command.sessionId());
        LoginSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new DomainException(DomainError.LOGIN_SESSION_NOT_FOUND));
        if (!session.getUserId().equals(new UserId(command.authenticatedUserId()))) {
            throw new DomainException(DomainError.LOGIN_SESSION_NOT_FOUND);
        }

        Instant now = clock.instant();
        SessionRevocationEffect effect = sessionDomainService.logout(session, now);
        sessionRepository.save(effect.session());
        loginSessionRevokers.forEach(revoker -> revoker.revoke(sessionId));
        domainEventStore.appendAll(effect.events());
        return new LogoutOutput(sessionId.value(), effect.session().getStatus());
    }
}
