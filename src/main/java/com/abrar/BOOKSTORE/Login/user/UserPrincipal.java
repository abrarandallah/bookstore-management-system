package com.abrar.BOOKSTORE.Login.user;

// class to represent the authenticated user
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserPrincipal implements UserDetails {

    @Getter
    private Long id;
    @Getter
    private String usernameOrEmail;
    @Getter
    private String email;
    @Getter
    private String avatarUrl;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    // Defaults true so the existing 6-arg constructor (used directly by
    // JwtTokenProviderTest) keeps behaving as before; UserPrincipal.create()
    // below is the only real caller and always sets this explicitly from
    // User.isVerified().
    private boolean verified = true;

    public UserPrincipal(Long id, String usernameOrEmail, String email, String avatarUrl, String password,
            Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.usernameOrEmail = usernameOrEmail;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.password = password;
        this.authorities = authorities;
    }

    public static UserDetails create(User user) {
        // Create a collection of authorities (roles) for the user
        Collection<SimpleGrantedAuthority> authorities = Collections
                .singleton(new SimpleGrantedAuthority(user.getRole()));

        // Create a new UserPrincipal instance using the user's details
        UserPrincipal principal = new UserPrincipal(
                user.getId(),
                user.getUsernameOrEmail(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getPassword(),
                authorities);
        principal.setVerified(user.isVerified());
        return principal;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUsernameOrEmail(String usernameOrEmail) {
        this.usernameOrEmail = usernameOrEmail;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return usernameOrEmail;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
        this.authorities = authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Gates both session (formLogin) and JWT (/api/auth/login) login,
        // since both go through the same DaoAuthenticationProvider fed by
        // CustomUserDetailsService.loadUserByUsername() -> this class.
        return verified;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
}