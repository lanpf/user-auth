package com.cloud.userauth.api.authorization;

import com.cloud.framework.core.Pagination;
import com.cloud.framework.core.Request;

public record AuthorizationCatalogApiQuery(
        Integer pageNo,
        Integer pageSize
) implements Request, Pagination {

    @Override
    public Integer getPageNo() {
        return pageNo;
    }

    @Override
    public Integer getPageSize() {
        return pageSize;
    }
}
