package com.cloud.userauth.infrastructure.common;

import com.cloud.framework.core.error.BaseError;
import com.cloud.framework.core.error.ErrorCodeNamespace;
import com.cloud.framework.core.error.ErrorCodePrefix;
import com.cloud.framework.core.error.ErrorCodeRange;
import lombok.Getter;

@Getter
public enum InfrastructureError implements BaseError {
    INFRA_TECH_LOCK_EXECUTION_FAILED(700, "分布式锁执行失败"),

    INFRA_ACL_EXTERNAL_IDENTITY_PROVIDER_UNAVAILABLE(800, "外部身份服务暂时不可用");

    private final int localCode;
    private final String message;

    InfrastructureError(int localCode, String message) {
        ErrorCodeRange.assertLocalCode(localCode);
        this.localCode = localCode;
        this.message = message;
    }

    @Override
    public ErrorCodePrefix getPrefix() {
        return new ErrorCodePrefix(ErrorCodeNamespace.BIZ, 0);
    }
}
