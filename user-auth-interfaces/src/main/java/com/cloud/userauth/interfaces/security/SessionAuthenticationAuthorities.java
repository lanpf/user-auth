package com.cloud.userauth.interfaces.security;

import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/** 区分宿主登录态与受限 浏览器 登录态的接口层认证权限。 */
public final class SessionAuthenticationAuthorities {
    private SessionAuthenticationAuthorities() {
    }
    public static final String HOST_SESSION = "AUTHORITY_HOST_SESSION";
    public static final String BROWSER_SESSION = "AUTHORITY_BROWSER_SESSION";

    public static List<GrantedAuthority> hostSession() {
        return List.of(new SimpleGrantedAuthority(HOST_SESSION));
    }

    public static List<GrantedAuthority> browserSession() {
        return List.of(new SimpleGrantedAuthority(BROWSER_SESSION));
    }
}
