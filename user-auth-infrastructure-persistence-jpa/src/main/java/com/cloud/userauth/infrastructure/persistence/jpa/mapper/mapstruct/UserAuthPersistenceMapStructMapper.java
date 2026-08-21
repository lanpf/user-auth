package com.cloud.userauth.infrastructure.persistence.jpa.mapper.mapstruct;

import com.cloud.framework.core.mapper.MapStructConfig;
import com.cloud.userauth.application.registration.RegistrationProcess;
import com.cloud.userauth.application.registration.RegistrationProcessId;
import com.cloud.userauth.application.registration.RegistrationProcessStatus;
import com.cloud.userauth.domain.authentication.account.AuthAccount;
import com.cloud.userauth.domain.authentication.account.AuthAccountId;
import com.cloud.userauth.domain.authentication.account.AuthAccountStatus;
import com.cloud.userauth.domain.authentication.challenge.AuthChallenge;
import com.cloud.userauth.domain.authentication.challenge.AuthChallengeId;
import com.cloud.userauth.domain.authentication.challenge.AuthChallengeScene;
import com.cloud.userauth.domain.authentication.challenge.AuthChallengeStatus;
import com.cloud.userauth.domain.authentication.challenge.AuthChallengeType;
import com.cloud.userauth.domain.authentication.challenge.ChallengeConsumerType;
import com.cloud.userauth.domain.authentication.challenge.ChallengeSecretHash;
import com.cloud.userauth.domain.authentication.challenge.ChallengeTarget;
import com.cloud.userauth.domain.authentication.credential.Credential;
import com.cloud.userauth.domain.authentication.credential.CredentialId;
import com.cloud.userauth.domain.authentication.credential.CredentialIssuer;
import com.cloud.userauth.domain.authentication.credential.CredentialIssuerType;
import com.cloud.userauth.domain.authentication.credential.CredentialStatus;
import com.cloud.userauth.domain.authentication.credential.CredentialType;
import com.cloud.userauth.domain.authentication.credential.Principal;
import com.cloud.userauth.domain.authentication.credential.LoginMobile;
import com.cloud.userauth.domain.authentication.loginattempt.LoginAttempt;
import com.cloud.userauth.domain.authentication.loginattempt.LoginAttemptId;
import com.cloud.userauth.domain.authentication.loginattempt.LoginAttemptStatus;
import com.cloud.userauth.domain.authentication.session.Client;
import com.cloud.userauth.domain.authentication.session.Device;
import com.cloud.userauth.domain.authentication.session.LoginScene;
import com.cloud.userauth.domain.authentication.session.LoginSession;
import com.cloud.userauth.domain.authentication.session.SessionId;
import com.cloud.userauth.domain.authentication.session.SessionStatus;
import com.cloud.userauth.domain.user.UserId;
import com.cloud.userauth.infrastructure.persistence.jpa.mapper.UserAuthPersistenceMapper;
import com.cloud.userauth.infrastructure.persistence.jpa.model.AuthAccountDO;
import com.cloud.userauth.infrastructure.persistence.jpa.model.AuthChallengeDO;
import com.cloud.userauth.infrastructure.persistence.jpa.model.CredentialDO;
import com.cloud.userauth.infrastructure.persistence.jpa.model.LoginSessionDO;
import com.cloud.userauth.infrastructure.persistence.jpa.model.LoginAttemptDO;
import com.cloud.userauth.infrastructure.persistence.jpa.model.RegistrationProcessDO;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface UserAuthPersistenceMapStructMapper extends UserAuthPersistenceMapper {
    @Override
    @Mapping(target = "id", source = "id.value")
    @Mapping(target = "userId", source = "userId.value")
    @Mapping(target = "status", source = "status")
    AuthAccountDO toDataObject(AuthAccount source);

    @Override
    @Mapping(target = "id", source = "source.credentialId.value")
    @Mapping(target = "authAccountId", source = "accountId.value")
    @Mapping(target = "credentialType", source = "source.credentialType")
    @Mapping(target = "issuer", source = "source.issuer.code")
    @Mapping(target = "issuerType", source = "source.issuer.issuerType")
    @Mapping(target = "principal", source = "source.principal.value")
    @Mapping(target = "status", source = "source.status")
    CredentialDO toDataObject(AuthAccountId accountId, Credential source);

    @Override
    @Mapping(target = "id", source = "id.value")
    @Mapping(target = "challengeType", source = "type")
    @Mapping(target = "target", source = "target.value")
    @Mapping(target = "scene", source = "scene")
    @Mapping(target = "secretHash", source = "secretHash.value")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "consumedByType", source = "consumedByType")
    AuthChallengeDO toDataObject(AuthChallenge source);

    @Override
    @Mapping(target = "id", source = "id.value")
    @Mapping(target = "challengeId", source = "challengeId.value")
    @Mapping(target = "userId", source = "userId.value")
    @Mapping(target = "authAccountId", source = "authAccountId.value")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "sessionId", source = "sessionId.value")
    RegistrationProcessDO toDataObject(RegistrationProcess source);

    @Override
    @Mapping(target = "id", source = "id.value")
    @Mapping(target = "userId", source = "userId.value")
    @Mapping(target = "authAccountId", source = "authAccountId.value")
    @Mapping(target = "authenticatedCredentialId", source = "authenticatedCredentialId.value")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "loginScene", source = "loginScene")
    @Mapping(target = "deviceId", source = "device.deviceId")
    @Mapping(target = "deviceType", source = "device.deviceType")
    @Mapping(target = "deviceName", source = "device.deviceName")
    @Mapping(target = "clientAppId", source = "client.appId")
    @Mapping(target = "clientPlatform", source = "client.platform")
    @Mapping(target = "clientVersion", source = "client.version")
    LoginSessionDO toDataObject(LoginSession source);

    @Override
    @Mapping(target = "id", source = "id.value")
    @Mapping(target = "issuer", source = "issuer.code")
    @Mapping(target = "issuerType", source = "issuer.issuerType")
    @Mapping(target = "principal", source = "principal.value")
    @Mapping(target = "mobile", source = "mobile.value")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "sessionId", source = "sessionId.value")
    LoginAttemptDO toDataObject(LoginAttempt source);

    @Override
    default AuthAccount toDomain(AuthAccountDO source, List<CredentialDO> credentials) {
        return AuthAccount.restore(
                new AuthAccountId(source.getId()),
                new UserId(source.getUserId()),
                AuthAccountStatus.valueOf(source.getStatus()),
                credentials.stream().map(this::toDomain).toList(),
                source.getCreatedAt(),
                source.getUpdatedAt());
    }

    @Override
    default Credential toDomain(CredentialDO source) {
        return Credential.restore(
                new CredentialId(source.getId()),
                CredentialType.valueOf(source.getCredentialType()),
                new CredentialIssuer(
                        source.getIssuer(),
                        CredentialIssuerType.valueOf(source.getIssuerType())),
                new Principal(source.getPrincipal()),
                CredentialStatus.valueOf(source.getStatus()),
                source.getVerifiedAt(),
                source.getCreatedAt(),
                source.getUpdatedAt());
    }

    @Override
    default AuthChallenge toDomain(AuthChallengeDO source) {
        return AuthChallenge.restore(
                new AuthChallengeId(source.getId()),
                AuthChallengeType.valueOf(source.getChallengeType()),
                new ChallengeTarget(source.getTarget()),
                AuthChallengeScene.valueOf(source.getScene()),
                new ChallengeSecretHash(source.getSecretHash()),
                AuthChallengeStatus.valueOf(source.getStatus()),
                source.getExpiresAt(),
                source.getReusableUntil(),
                source.getAttempts(),
                source.getVerifiedAt(),
                enumValue(ChallengeConsumerType.class, source.getConsumedByType()),
                source.getConsumedById(),
                source.getCreatedAt(),
                source.getUpdatedAt());
    }

    @Override
    default RegistrationProcess toDomain(RegistrationProcessDO source) {
        return RegistrationProcess.restore(
                new RegistrationProcessId(source.getId()),
                new AuthChallengeId(source.getChallengeId()),
                new UserId(source.getUserId()),
                new AuthAccountId(source.getAuthAccountId()),
                RegistrationProcessStatus.valueOf(source.getStatus()),
                source.getLastFailure(),
                source.getRetryCount(),
                source.getLastFailedAt(),
                sessionId(source.getSessionId()),
                source.getCreatedAt(),
                source.getUpdatedAt());
    }

    @Override
    default LoginSession toDomain(LoginSessionDO source) {
        return LoginSession.restore(
                new SessionId(source.getId()),
                new UserId(source.getUserId()),
                new AuthAccountId(source.getAuthAccountId()),
                new CredentialId(source.getAuthenticatedCredentialId()),
                SessionStatus.valueOf(source.getStatus()),
                LoginScene.valueOf(source.getLoginScene()),
                new Device(source.getDeviceId(), source.getDeviceType(), source.getDeviceName()),
                new Client(
                        source.getClientAppId(),
                        source.getClientPlatform(),
                        source.getClientVersion()),
                source.getIssuedAt(),
                source.getExpiresAt(),
                source.getLastActiveAt());
    }

    @Override
    default LoginAttempt toDomain(LoginAttemptDO source) {
        return LoginAttempt.restore(
                new LoginAttemptId(source.getId()),
                new CredentialIssuer(
                        source.getIssuer(),
                        CredentialIssuerType.valueOf(source.getIssuerType())),
                new Principal(source.getPrincipal()),
                loginMobile(source.getMobile()),
                source.isMobileVerified(),
                LoginAttemptStatus.valueOf(source.getStatus()),
                sessionId(source.getSessionId()),
                source.getExpiresAt(),
                source.getCreatedAt(),
                source.getUpdatedAt());
    }

    private static <E extends Enum<E>> E enumValue(Class<E> type, String value) {
        return value == null ? null : Enum.valueOf(type, value);
    }

    private static SessionId sessionId(String value) {
        return value == null ? null : new SessionId(value);
    }

    private static LoginMobile loginMobile(String value) {
        return value == null ? null : new LoginMobile(value);
    }

}
