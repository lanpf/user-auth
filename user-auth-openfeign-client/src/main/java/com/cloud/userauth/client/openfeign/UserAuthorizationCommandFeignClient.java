package com.cloud.userauth.client.openfeign;

import com.cloud.framework.core.Result;
import com.cloud.userauth.api.authorization.ReconcileChannelAuthorizationPolicyApiCommand;
import com.cloud.userauth.api.authorization.ReconcileChannelAuthorizationPolicyApiCommandOutput;
import com.cloud.userauth.api.constants.UserAuthPathApiConstants;
import com.cloud.userauth.api.facade.UserAuthorizationCommandFacade;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = UserAuthPathApiConstants.SERVICE_NAME, contextId = "userAuthorizationCommandFeignClient")
public interface UserAuthorizationCommandFeignClient extends UserAuthorizationCommandFacade {
    @Override
    @PostMapping(UserAuthPathApiConstants.ADMIN_AUTHORIZATION_CHANNEL_POLICIES_RECONCILIATION)
    Result<ReconcileChannelAuthorizationPolicyApiCommandOutput> reconcileChannelPolicy(
            @RequestBody ReconcileChannelAuthorizationPolicyApiCommand request
    );
}
