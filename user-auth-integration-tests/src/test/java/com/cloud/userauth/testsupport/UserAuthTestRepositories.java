package com.cloud.userauth.testsupport;

import com.cloud.userauth.application.registration.RegistrationProcess;
import com.cloud.userauth.application.registration.RegistrationProcessId;
import com.cloud.userauth.application.registration.RegistrationProcessRepository;
import com.cloud.userauth.domain.authentication.account.AuthAccount;
import com.cloud.userauth.domain.authentication.account.AuthAccountId;
import com.cloud.userauth.domain.authentication.account.AuthAccountRepository;
import com.cloud.userauth.domain.authentication.challenge.*;
import com.cloud.userauth.domain.authentication.credential.CredentialKey;
import com.cloud.userauth.domain.authentication.credential.CredentialIssuer;
import com.cloud.userauth.domain.authentication.credential.Principal;
import com.cloud.userauth.domain.authentication.loginattempt.LoginAttempt;
import com.cloud.userauth.domain.authentication.loginattempt.LoginAttemptId;
import com.cloud.userauth.domain.authentication.loginattempt.LoginAttemptRepository;
import com.cloud.userauth.domain.authentication.session.*;
import com.cloud.userauth.domain.user.UserId;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public final class UserAuthTestRepositories {
    private UserAuthTestRepositories() {
    }

    public static final class Accounts implements AuthAccountRepository {
        private final AtomicLong sequence = new AtomicLong(1000);
        private final Map<Long, AuthAccount> values = new ConcurrentHashMap<>();

        @Override public AuthAccountId nextId() { return new AuthAccountId(sequence.incrementAndGet()); }
        @Override public void save(AuthAccount value) { values.put(value.id().value(), value); }
        @Override public Optional<AuthAccount> findById(AuthAccountId id) { return Optional.ofNullable(values.get(id.value())); }
        @Override public Optional<AuthAccount> findByUserId(UserId id) { return values.values().stream().filter(value -> value.userId().equals(id)).findFirst(); }
        @Override public Optional<AuthAccount> findByCredential(CredentialKey key) { return values.values().stream().filter(value -> value.hasActiveCredential(key)).findFirst(); }
        @Override public boolean existsActiveByUserId(UserId id) { return findByUserId(id).isPresent(); }
        @Override public boolean existsActiveCredential(CredentialKey key) { return findByCredential(key).isPresent(); }
    }

    public static final class Challenges implements AuthChallengeRepository {
        private final AtomicLong sequence = new AtomicLong(1000);
        private final Map<Long, AuthChallenge> values = new ConcurrentHashMap<>();

        @Override public AuthChallengeId nextId() { return new AuthChallengeId(sequence.incrementAndGet()); }
        @Override public void save(AuthChallenge value) { values.put(value.id().value(), value); }
        @Override public Optional<AuthChallenge> findById(AuthChallengeId id) { return Optional.ofNullable(values.get(id.value())); }
        @Override
        public Optional<AuthChallenge> findReusable(
                AuthChallengeType type,
                ChallengeTarget target,
                AuthChallengeScene scene,
                Instant now
        ) {
            return values.values().stream()
                    .filter(value -> value.getType() == type && value.getTarget().equals(target) && value.getScene() == scene)
                    .filter(value -> value.isReusableAt(now))
                    .max(Comparator.comparing(AuthChallenge::getCreatedAt));
        }
    }

    public static final class Registrations implements RegistrationProcessRepository {
        private final AtomicLong sequence = new AtomicLong(1000);
        private final Map<Long, RegistrationProcess> values = new ConcurrentHashMap<>();

        @Override public RegistrationProcessId nextId() { return new RegistrationProcessId(sequence.incrementAndGet()); }
        @Override public void save(RegistrationProcess value) { values.put(value.id().value(), value); }
        @Override public Optional<RegistrationProcess> findById(RegistrationProcessId id) { return Optional.ofNullable(values.get(id.value())); }
        @Override public Optional<RegistrationProcess> findByChallengeId(AuthChallengeId id) { return values.values().stream().filter(value -> value.getChallengeId().equals(id)).findFirst(); }
    }

    public static final class Sessions implements LoginSessionRepository {
        private final Map<String, LoginSession> values = new ConcurrentHashMap<>();
        @Override public SessionId nextId() { return new SessionId(UUID.randomUUID().toString()); }
        @Override public void save(LoginSession value) { values.put(value.id().value(), value); }
        @Override public Optional<LoginSession> findById(SessionId id) { return Optional.ofNullable(values.get(id.value())); }
        @Override public List<LoginSession> findActiveByUserId(UserId id) { return values.values().stream().filter(value -> value.getUserId().equals(id) && value.getStatus() == SessionStatus.ACTIVE).toList(); }
        @Override public List<LoginSession> findActiveByUserIdAndDevice(UserId id, String deviceId) { return findActiveByUserId(id).stream().filter(value -> value.getDevice() != null && Objects.equals(value.getDevice().deviceId(), deviceId)).toList(); }
    }

    public static final class LoginAttempts implements LoginAttemptRepository {
        private final Map<String, LoginAttempt> values = new ConcurrentHashMap<>();

        @Override
        public LoginAttemptId nextId() {
            return new LoginAttemptId(UUID.randomUUID().toString());
        }

        @Override
        public void save(LoginAttempt value) {
            values.put(value.id().value(), value);
        }

        @Override
        public Optional<LoginAttempt> findById(LoginAttemptId id) {
            return Optional.ofNullable(values.get(id.value()));
        }

        @Override
        public Optional<LoginAttempt> findPendingByIssuerAndExternalPrincipal(
                CredentialIssuer issuer,
                Principal externalPrincipal
        ) {
            return values.values().stream()
                    .filter(value -> value.getIssuer().equals(issuer)
                            && value.getExternalPrincipal().equals(externalPrincipal)
                            && (value.getStatus()
                            == com.cloud.userauth.domain.authentication.loginattempt.LoginAttemptStatus.PENDING_MOBILE
                            || value.getStatus()
                            == com.cloud.userauth.domain.authentication.loginattempt.LoginAttemptStatus.READY))
                    .findFirst();
        }
    }
}
