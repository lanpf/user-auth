package com.cloud.userauth.infrastructure.oauth2.sas.config;

import com.cloud.framework.core.http.RestClientProperties;
import com.cloud.userauth.api.authentication.OAuth2Scope;
import com.cloud.userauth.infrastructure.oauth2.sas.config.validation.LoopbackTokenEndpoint;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties("user-auth.authentication.oauth2.authorization-server.sas")
public class SasAuthorizationServerProperties {
    @Valid
    private final InternalTokenClientProperties internalTokenClient = new InternalTokenClientProperties();
    @Valid
    private final RefreshTokenProperties refreshToken = new RefreshTokenProperties();
    @NotBlank
    private String issuer = "http://localhost:8081";
    @NotEmpty
    private final Set<@NotNull OAuth2Scope> scopes = new LinkedHashSet<>();

    @NotEmpty
    private final Set<@NotBlank String> audiences = new LinkedHashSet<>();
    @Valid
    private final OidcProperties oidc = new OidcProperties();

    @Getter
    @Setter
    public static class InternalTokenClientProperties {
        @NotBlank
        private String clientId;
        @NotBlank
        private String clientSecret;
        @LoopbackTokenEndpoint
        private String tokenEndpoint;
        @Valid
        private final RestClientProperties restClient = new RestClientProperties();
    }

    @Getter
    @Setter
    public static class RefreshTokenProperties {
        @NotNull
        @DurationMin(minutes = 10)
        private Duration ttl = Duration.ofDays(30);
    }

    /**
     * OIDC 协议端点开关。它只控制 SAS 的协议能力，不决定某个客户端是否可使用
     * {@code openid} scope；后者必须由 RegisteredClient 的 scope 配置决定。
     */
    @Getter
    @Setter
    public static class OidcProperties {
        private boolean enabled;
    }

    @Getter
    @Setter
    @Validated
    @ConfigurationProperties("user-auth.authentication.oauth2.authorization-server.sas.signature")
    public static class SignatureProperties {
        @Valid
        private final KeyStoreProperties keyStore = new KeyStoreProperties();
        @NotBlank
        private String keyAlias;
        private String keyPassword;
        private String keyId;

        public String getKeyPassword() {
            return StringUtils.hasText(keyPassword) ? keyPassword : keyStore.getPassword();
        }

        public String getKeyId() {
            return StringUtils.hasText(keyId) ? keyId : keyAlias;
        }
    }

    @Getter
    @Setter
    public static class KeyStoreProperties {
        @NotBlank
        private String type;
        @NotBlank
        private String location;
        @NotBlank
        private String password;
    }
}
