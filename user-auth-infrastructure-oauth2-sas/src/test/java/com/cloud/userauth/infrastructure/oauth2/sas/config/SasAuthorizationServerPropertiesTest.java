package com.cloud.userauth.infrastructure.oauth2.sas.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cloud.userauth.api.authentication.OAuth2Scope;
import com.cloud.userauth.infrastructure.oauth2.sas.config.validation.LoopbackTokenEndpoint;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.lang.annotation.Annotation;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import org.hibernate.validator.constraints.time.DurationMin;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.BindException;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

class SasAuthorizationServerPropertiesTest {
    private static final Validator VALIDATOR =
            Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldNotProvideBusinessCollectionDefaults() {
        SasAuthorizationServerProperties properties =
                SasAuthorizationServerPropertiesFixtures.defaults();

        assertTrue(properties.getScopes().isEmpty());
        assertTrue(properties.getAudiences().isEmpty());
    }

    @Test
    void shouldBindGetterOnlyFinalCollections() {
        MapConfigurationPropertySource source =
                new MapConfigurationPropertySource(Map.of(
                        "user-auth.authentication.oauth2.authorization-server.sas.scopes[0]",
                        "app",
                        "user-auth.authentication.oauth2.authorization-server.sas.scopes[1]",
                        "admin",
                        "user-auth.authentication.oauth2.authorization-server.sas.internal-token-client.client-id",
                        "internal-client",
                        "user-auth.authentication.oauth2.authorization-server.sas.internal-token-client.client-secret",
                        "internal-secret",
                        "user-auth.authentication.oauth2.authorization-server.sas.refresh-token.ttl",
                        "7d",
                        "user-auth.authentication.oauth2.authorization-server.sas.audiences[0]",
                        "cloud-platform-api"));

        SasAuthorizationServerProperties properties = new Binder(source)
                .bind(
                        "user-auth.authentication.oauth2.authorization-server.sas",
                        Bindable.of(SasAuthorizationServerProperties.class))
                .orElseThrow(IllegalStateException::new);

        assertEquals(
                Set.of(OAuth2Scope.APP, OAuth2Scope.ADMIN),
                properties.getScopes());
        assertEquals("internal-client", properties.getInternalTokenClient().getClientId());
        assertEquals("internal-secret", properties.getInternalTokenClient().getClientSecret());
        assertEquals(Duration.ofDays(7), properties.getRefreshToken().getTtl());
        assertEquals(
                Set.of("cloud-platform-api"),
                properties.getAudiences());
    }

    @Test
    void shouldUseKeyStorePasswordWhenKeyPasswordIsBlank() {
        SasAuthorizationServerProperties.SignatureProperties properties =
                new SasAuthorizationServerProperties.SignatureProperties();
        properties.getKeyStore().setPassword("store-password");

        assertEquals("store-password", properties.getKeyPassword());
    }

    @Test
    void shouldPreferKeyPasswordWhenConfigured() {
        SasAuthorizationServerProperties.SignatureProperties properties =
                new SasAuthorizationServerProperties.SignatureProperties();
        properties.getKeyStore().setPassword("store-password");
        properties.setKeyPassword("key-password");

        assertEquals("key-password", properties.getKeyPassword());
    }

    @Test
    void shouldUseKeyAliasWhenKeyIdIsBlank() {
        SasAuthorizationServerProperties.SignatureProperties properties =
                new SasAuthorizationServerProperties.SignatureProperties();
        properties.setKeyAlias("signing-key");

        assertEquals("signing-key", properties.getKeyId());
    }

    @Test
    void shouldPreferKeyIdWhenConfigured() {
        SasAuthorizationServerProperties.SignatureProperties properties =
                new SasAuthorizationServerProperties.SignatureProperties();
        properties.setKeyAlias("signing-key");
        properties.setKeyId("oauth2-key");

        assertEquals("oauth2-key", properties.getKeyId());
    }

    @Test
    void shouldNotProvideKeyStoreTypeDefault() {
        SasAuthorizationServerProperties.KeyStoreProperties properties =
                new SasAuthorizationServerProperties.KeyStoreProperties();

        assertNull(properties.getType());
    }

    @Test
    void shouldBindSignatureAndNestedKeyStore() {
        MapConfigurationPropertySource source = new MapConfigurationPropertySource(Map.of(
                "user-auth.authentication.oauth2.authorization-server.sas.signature.key-store.type", "PKCS12",
                "user-auth.authentication.oauth2.authorization-server.sas.signature.key-store.location", "file:/keys/signing.p12",
                "user-auth.authentication.oauth2.authorization-server.sas.signature.key-store.password", "store-password",
                "user-auth.authentication.oauth2.authorization-server.sas.signature.key-alias", "signing-key"));

        SasAuthorizationServerProperties.SignatureProperties properties = new Binder(source)
                .bind(
                        "user-auth.authentication.oauth2.authorization-server.sas.signature",
                        Bindable.of(SasAuthorizationServerProperties.SignatureProperties.class))
                .orElseThrow(IllegalStateException::new);

        assertEquals("PKCS12", properties.getKeyStore().getType());
        assertEquals("file:/keys/signing.p12", properties.getKeyStore().getLocation());
        assertEquals("store-password", properties.getKeyStore().getPassword());
        assertEquals("signing-key", properties.getKeyAlias());
        assertEquals("store-password", properties.getKeyPassword());
    }

    @Test
    void shouldRejectNonPositiveInternalHttpTimeout() {
        MapConfigurationPropertySource source =
                new MapConfigurationPropertySource(Map.of(
                        "user-auth.authentication.oauth2.authorization-server.sas.internal-token-client.rest-client.read-timeout",
                        Duration.ZERO));
        SasAuthorizationServerProperties properties = new Binder(source)
                .bind(
                        "user-auth.authentication.oauth2.authorization-server.sas",
                        Bindable.of(SasAuthorizationServerProperties.class))
                .orElseThrow(IllegalStateException::new);

        assertTrue(hasViolation(properties, DurationMin.class));
    }

    @Test
    void shouldBindSharedTimeoutNamesAndDefaults() {
        SasAuthorizationServerProperties defaults =
                SasAuthorizationServerPropertiesFixtures.defaults();
        MapConfigurationPropertySource source =
                new MapConfigurationPropertySource(Map.of(
                        "user-auth.authentication.oauth2.authorization-server.sas.internal-token-client.rest-client.connect-timeout",
                        "3s",
                        "user-auth.authentication.oauth2.authorization-server.sas.internal-token-client.rest-client.read-timeout",
                        "8s"));
        SasAuthorizationServerProperties configured = new Binder(source)
                .bind(
                        "user-auth.authentication.oauth2.authorization-server.sas",
                        Bindable.of(SasAuthorizationServerProperties.class))
                .orElseThrow(IllegalStateException::new);

        assertEquals(
                Duration.ofSeconds(2),
                defaults.getInternalTokenClient().getRestClient().getConnectTimeout());
        assertEquals(
                Duration.ofSeconds(5),
                defaults.getInternalTokenClient().getRestClient().getReadTimeout());
        assertEquals(
                Duration.ofSeconds(3),
                configured.getInternalTokenClient().getRestClient().getConnectTimeout());
        assertEquals(
                Duration.ofSeconds(8),
                configured.getInternalTokenClient().getRestClient().getReadTimeout());
    }

    @Test
    void shouldBindOidcProtocolCapabilityIndependentlyFromSasProvider() {
        MapConfigurationPropertySource source = new MapConfigurationPropertySource(Map.of(
                "user-auth.authentication.oauth2.authorization-server.sas.oidc.enabled", true));

        SasAuthorizationServerProperties properties = new Binder(source)
                .bind(
                        "user-auth.authentication.oauth2.authorization-server.sas",
                        Bindable.of(SasAuthorizationServerProperties.class))
                .orElseThrow(IllegalStateException::new);

        assertTrue(properties.getOidc().isEnabled());
    }

    @Test
    void shouldRejectUnknownOAuth2ScopeDuringBinding() {
        MapConfigurationPropertySource source =
                new MapConfigurationPropertySource(Map.of(
                        "user-auth.authentication.oauth2.authorization-server.sas.scopes[0]",
                        "SCOPE_app"));

        assertThrows(BindException.class, () -> new Binder(source).bind(
                "user-auth.authentication.oauth2.authorization-server.sas",
                Bindable.of(SasAuthorizationServerProperties.class)));
    }

    @Test
    void shouldRejectNonLoopbackInternalTokenEndpoint() {
        SasAuthorizationServerProperties properties =
                SasAuthorizationServerPropertiesFixtures
                        .withInternalTokenEndpoint(
                                "https://auth.example.com/oauth2/token");

        assertTrue(hasViolation(properties, LoopbackTokenEndpoint.class));
    }

    private static boolean hasViolation(
            SasAuthorizationServerProperties properties,
            Class<? extends Annotation> annotationType
    ) {
        return VALIDATOR.validate(properties).stream()
                .map(ConstraintViolation::getConstraintDescriptor)
                .map(descriptor -> descriptor.getAnnotation().annotationType())
                .anyMatch(annotationType::equals);
    }

}
