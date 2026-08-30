package com.cloud.userauth.infrastructure.persistence.jpa.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import java.io.Serializable;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@IdClass(ChannelAuthorizationPolicyPermissionDO.Key.class)
public class ChannelAuthorizationPolicyPermissionDO {
    @Id
    private String channelCode;
    @Id
    private String permissionCode;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Key implements Serializable {
        private String channelCode;
        private String permissionCode;

        @Override
        public boolean equals(Object other) {
            return other instanceof Key key
                    && Objects.equals(channelCode, key.channelCode)
                    && Objects.equals(permissionCode, key.permissionCode);
        }

        @Override
        public int hashCode() {
            return Objects.hash(channelCode, permissionCode);
        }
    }
}
