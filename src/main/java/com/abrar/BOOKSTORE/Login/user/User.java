package com.abrar.BOOKSTORE.Login.user;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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

    // i guess this field should go to User entity
    @Getter
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
}