package com.cloud.userauth.interfaces.security;

import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/** 区分宿主登录态与受限 H5 登录态的接口层认证权限。 */
public final class SessionAuthenticationAuthorities {
    private SessionAuthenticationAuthorities() {
    }
    public static final String HOST_SESSION = "USER_AUTH_HOST_SESSION";
    public static final String H5_SESSION = "USER_AUTH_H5_SESSION";

    public static List<GrantedAuthority> hostSession() {
        return List.of(new SimpleGrantedAuthority(HOST_SESSION));
    }

    public static List<GrantedAuthority> h5Session() {
        return List.of(new SimpleGrantedAuthority(H5_SESSION));
    }
}
