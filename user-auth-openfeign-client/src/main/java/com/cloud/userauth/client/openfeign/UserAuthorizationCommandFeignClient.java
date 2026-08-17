package com.cloud.userauth.client.openfeign;

import com.cloud.framework.core.Result;
import com.cloud.userauth.api.authorization.ReconcileChannelAuthorizationPolicyApiCommand;
import com.cloud.userauth.api.authorization.ReconcileChannelAuthorizationPolicyApiCommandOutput;
import com.cloud.userauth.api.facade.UserAuthorizationCommandFacade;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-auth", contextId = "userAuthorizationCommandFeignClient",
        path = "/api/user-auth/admin/authorization")
public interface UserAuthorizationCommandFeignClient extends UserAuthorizationCommandFacade {
    @Override
    @PostMapping("/channel-policies/reconciliation")
    Result<ReconcileChannelAuthorizationPolicyApiCommandOutput> reconcileChannelPolicy(
            @RequestBody ReconcileChannelAuthorizationPolicyApiCommand request
    );
}
