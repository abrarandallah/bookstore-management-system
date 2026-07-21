package com.abrar.BOOKSTORE.Login.user;

import jakarta.persistence.*;
import lombok.Getter;
import org.aspectj.bridge.Message;
import org.springframework.security.core.GrantedAuthority;

@Entity
@Table(name="roles")
public class Role implements GrantedAuthority {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer roleID;
    private String authority;

    @Override
    public String getAuthority() {
        return this.authority;
    }

    public Role() {
    }

    public Role(Integer roleID, String authority) {
        this.roleID = roleID;
        this.authority = authority;
    }

    public void setRoleID(Integer roleID) {
        this.roleID = roleID;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }
}
