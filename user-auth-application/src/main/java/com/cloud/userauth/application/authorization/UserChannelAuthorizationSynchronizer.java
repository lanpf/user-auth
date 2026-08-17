package com.cloud.userauth.application.authorization;

import com.cloud.userauth.domain.user.UserId;

/**
 * 在用户通过某个渠道完成认证后，同步该渠道来源的功能授权。
 */
@FunctionalInterface
public interface UserChannelAuthorizationSynchronizer {
    void synchronize(UserId userId, String channelCode);
}
