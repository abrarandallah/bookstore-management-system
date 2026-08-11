package com.abrar.BOOKSTORE.Login.user;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Getter
    @Column(nullable = true, unique = true)
    private String usernameOrEmail;
    @Getter
    @Column(nullable = true, unique = true)
    private String email;
    @Column(nullable = true)
    private String password;
    @Getter
    private String role;
    @Getter
    private String avatarUrl;
    // Defaults to true so existing rows are grandfathered in as verified when
    // this column is first added to the DB (see columnDefinition). New
    // signups explicitly flip this to false in AuthService.registerUser and
    // require clicking the link from EmailService.sendVerificationEmail
    // before they can log in - see UserPrincipal.isEnabled().
    @Getter
    @Column(nullable = false, columnDefinition = "TINYINT(1) NOT NULL DEFAULT 1")
    private boolean verified = true;

    public User() {
    }

    public User(String username, String email, String encode, String roleUser) {
        this.usernameOrEmail = username;
        this.email = email;
        this.password = encode;
        this.role = roleUser;
    }

    public User(long id, String usernameOrEmail, String password, String role) {
        this.id = id;
        this.usernameOrEmail = usernameOrEmail;
        this.password = password;
        this.role = role;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setUsernameOrEmail(String usernameOrEmail) {
        this.usernameOrEmail = usernameOrEmail;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public void setRole(String role) {
        this.role = role;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
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
        return true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Mirrors UserPrincipal.create(): a single authority derived from the
        // simple `role` string (e.g. "ROLE_USER" / "ROLE_LIBRARIAN").
        if (role == null || role.isBlank()) {
            return Collections.emptyList();
        }
        return Collections.singleton(new SimpleGrantedAuthority(role));
    }
}