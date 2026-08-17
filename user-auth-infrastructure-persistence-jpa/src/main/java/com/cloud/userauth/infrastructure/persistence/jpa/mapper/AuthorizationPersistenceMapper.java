package com.cloud.userauth.infrastructure.persistence.jpa.mapper;

import com.cloud.userauth.domain.authorization.ChannelAuthorizationPolicy;
import com.cloud.userauth.domain.authorization.Permission;
import com.cloud.userauth.domain.authorization.Role;
import com.cloud.userauth.domain.authorization.UserChannelPolicyApplication;
import com.cloud.userauth.domain.authorization.UserPermissionGrant;
import com.cloud.userauth.domain.authorization.UserRoleGrant;
import com.cloud.userauth.infrastructure.persistence.jpa.model.ChannelAuthorizationPolicyDO;
import com.cloud.userauth.infrastructure.persistence.jpa.model.PermissionDO;
import com.cloud.userauth.infrastructure.persistence.jpa.model.RoleDO;
import com.cloud.userauth.infrastructure.persistence.jpa.model.UserChannelPolicyApplicationDO;
import com.cloud.userauth.infrastructure.persistence.jpa.model.UserPermissionGrantDO;
import com.cloud.userauth.infrastructure.persistence.jpa.model.UserRoleGrantDO;

public interface AuthorizationPersistenceMapper {
    RoleDO toDataObject(Role source);
    Role toDomain(RoleDO source);
    PermissionDO toDataObject(Permission source);
    Permission toDomain(PermissionDO source);
    UserRoleGrantDO toDataObject(UserRoleGrant source);
    UserRoleGrant toDomain(UserRoleGrantDO source);
    UserPermissionGrantDO toDataObject(UserPermissionGrant source);
    UserPermissionGrant toDomain(UserPermissionGrantDO source);
    ChannelAuthorizationPolicyDO toDataObject(ChannelAuthorizationPolicy source);
    ChannelAuthorizationPolicy toDomain(ChannelAuthorizationPolicyDO source);
    UserChannelPolicyApplicationDO toDataObject(UserChannelPolicyApplication source);
    UserChannelPolicyApplication toDomain(UserChannelPolicyApplicationDO source);
}
