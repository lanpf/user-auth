package com.cloud.userauth.application.challenge;

import com.cloud.userauth.application.port.AuthChallengeDispatcher;
import com.cloud.userauth.application.port.AuthChallengeIssueLock;
import com.cloud.userauth.application.port.AuthChallengeIssuePolicy;
import com.cloud.userauth.application.port.AuthChallengePolicyProvider;
import com.cloud.userauth.application.port.ChallengeSecretHasher;
import com.cloud.userauth.application.port.OneTimeCodeGenerator;
import com.cloud.userauth.domain.authentication.challenge.AuthChallenge;
import com.cloud.userauth.domain.authentication.challenge.AuthChallengeId;
import com.cloud.userauth.domain.authentication.challenge.AuthChallengeRepository;
import com.cloud.userauth.domain.authentication.challenge.ChallengeTarget;
import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AuthChallengeIssueCommandService {
    private final AuthChallengeRepository repository;
    private final OneTimeCodeGenerator codeGenerator;
    private final ChallengeSecretHasher secretHasher;
    private final AuthChallengeDispatcher dispatcher;
    private final AuthChallengeIssueLock issueLock;
    private final AuthChallengePolicyProvider policyProvider;
    private final Clock clock;

    public IssueAuthChallengeOutput execute(IssueAuthChallengeCommand command) {
        ChallengeTarget target = ChallengeTarget.of(command.challengeType(), command.target());
        return issueLock.execute(command.challengeType(), command.scene(),
                target, () -> executeLocked(command, target));
    }

    private IssueAuthChallengeOutput executeLocked(
            IssueAuthChallengeCommand command,
            ChallengeTarget target
    ) {
        Instant now = clock.instant();
        return repository.findReusable(
                        command.challengeType(), target, command.scene(), now
                )
                .map(challenge -> new IssueAuthChallengeOutput(
                        challenge.id().value(), challenge.getExpiresAt(), true
                ))
                .orElseGet(() -> issueNew(command, target, now));
    }

    private IssueAuthChallengeOutput issueNew(
            IssueAuthChallengeCommand command,
            ChallengeTarget target,
            Instant now
    ) {
        AuthChallengeIssuePolicy policy = policyProvider.currentPolicy();
        String code = codeGenerator.generate();
        AuthChallengeId challengeId = repository.nextId();
        AuthChallenge challenge = AuthChallenge.issue(
                challengeId,
                command.challengeType(),
                target,
                command.scene(),
                secretHasher.hash(challengeId, code),
                now,
                now.plus(policy.ttl()),
                now.plus(policy.reuseWindow())
        );
        repository.save(challenge);
        dispatcher.dispatch(command.challengeType(), target, code);
        return new IssueAuthChallengeOutput(challenge.id().value(), challenge.getExpiresAt(), false);
    }
}
