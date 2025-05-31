package org.onlineshop.db;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final Integer id;
    private final String username;
    private final String password;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> auth;

    public CustomUserDetails(Integer id,
                             String username,
                             String password,
                             boolean enabled,
                             Collection<? extends GrantedAuthority> auth) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.enabled = enabled;
        this.auth = auth;
    }

    public Integer id() { return id; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return auth;
    }

    @Override public String getPassword() { return password; }

    @Override public String getUsername() { return username; }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return enabled;
    }


    @Override
    public boolean isCredentialsNonExpired() {
        return enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
