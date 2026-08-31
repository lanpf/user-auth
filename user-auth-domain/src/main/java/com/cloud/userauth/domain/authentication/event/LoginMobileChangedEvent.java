package com.cloud.userauth.domain.authentication.event;

import com.cloud.userauth.domain.authentication.account.AuthAccountId;
import com.cloud.framework.domain.AbstractDomainEvent;
import com.cloud.userauth.domain.authentication.credential.LoginMobile;
import com.cloud.userauth.domain.user.UserId;
import java.time.Instant;
import lombok.Getter;

@Getter
public class LoginMobileChangedEvent extends AbstractDomainEvent {
    private final UserId userId;
    private final AuthAccountId authAccountId;
    private final LoginMobile oldMobile;
    private final LoginMobile newMobile;

    public LoginMobileChangedEvent(
            Instant occurredAt,
            UserId userId,
            AuthAccountId authAccountId,
            LoginMobile oldMobile,
            LoginMobile newMobile
    ) {
        super(occurredAt);
        this.userId = userId;
        this.authAccountId = authAccountId;
        this.oldMobile = oldMobile;
        this.newMobile = newMobile;
    }
}
