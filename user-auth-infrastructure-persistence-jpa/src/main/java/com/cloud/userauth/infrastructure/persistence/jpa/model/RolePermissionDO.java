package com.cloud.userauth.infrastructure.persistence.jpa.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import java.io.Serializable;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@IdClass(RolePermissionDO.Key.class)
public class RolePermissionDO {
    @Id
    private String roleCode;
    @Id
    private String permissionCode;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Key implements Serializable {
        private String roleCode;
        private String permissionCode;

        public Key(String roleCode, String permissionCode) {
            this.roleCode = roleCode;
            this.permissionCode = permissionCode;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Key key
                    && Objects.equals(roleCode, key.roleCode)
                    && Objects.equals(permissionCode, key.permissionCode);
        }

        @Override
        public int hashCode() {
            return Objects.hash(roleCode, permissionCode);
        }
    }
}
