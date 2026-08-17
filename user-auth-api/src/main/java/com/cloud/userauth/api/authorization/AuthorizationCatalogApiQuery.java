package com.cloud.userauth.api.authorization;

import com.cloud.framework.core.PaginationRequest;

public record AuthorizationCatalogApiQuery(
        Integer pageNo,
        Integer pageSize
) implements PaginationRequest {

    @Override
    public Integer getPageNo() {
        return pageNo;
    }

    @Override
    public Integer getPageSize() {
        return pageSize;
    }
}
