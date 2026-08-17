package com.cloud.userauth.application.port;

public interface ClientRenewalPolicyResolver {
    ClientRenewalPolicy resolve(String clientAppId);
}
