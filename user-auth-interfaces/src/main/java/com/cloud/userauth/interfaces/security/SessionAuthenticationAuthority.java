package com.cloud.userauth.interfaces.security;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/** 区分宿主登录态与受限浏览器登录态的接口层认证权限。 */
@Getter
@RequiredArgsConstructor
public enum SessionAuthenticationAuthority {
    HOST_SESSION("AUTHORITY_HOST_SESSION"),
    BROWSER_SESSION("AUTHORITY_BROWSER_SESSION");

    private final String value;

    public List<GrantedAuthority> authorities() {
        return List.of(new SimpleGrantedAuthority(value));
    }
}
