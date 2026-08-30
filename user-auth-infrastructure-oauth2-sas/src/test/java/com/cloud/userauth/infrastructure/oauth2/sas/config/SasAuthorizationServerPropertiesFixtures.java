package com.cloud.userauth.infrastructure.oauth2.sas.config;

public final class SasAuthorizationServerPropertiesFixtures {
    private SasAuthorizationServerPropertiesFixtures() {
    }

    public static SasAuthorizationServerProperties defaults() {
        return create(null, null, null);
    }

    public static SasAuthorizationServerProperties withInternalClient(
            String clientId,
            String clientSecret
    ) {
        return create(clientId, clientSecret, null);
    }

    public static SasAuthorizationServerProperties
            withInternalTokenEndpoint(String tokenEndpoint) {
        return create(null, null, tokenEndpoint);
    }

    private static SasAuthorizationServerProperties create(
            String clientId,
            String clientSecret,
            String tokenEndpoint
    ) {
        SasAuthorizationServerProperties properties =
                new SasAuthorizationServerProperties();
        properties.getInternalTokenClient().setClientId(clientId);
        properties.getInternalTokenClient().setClientSecret(clientSecret);
        properties.getInternalTokenClient().setTokenEndpoint(tokenEndpoint);
        return properties;
    }

}
