package com.abrar.BOOKSTORE.Login.conf;

import lombok.Getter;


@Getter
public class LoginRequest {
    private String usernameOrEmail;
    private String password;

    // Constructors, getters, and setters


    public LoginRequest() {
    }

    public LoginRequest(String usernameOrEmail, String password) {
        this.usernameOrEmail = usernameOrEmail;
        this.password = password;
    }

    public void setUsernameOrEmail(String usernameOrEmail) {
        this.usernameOrEmail = usernameOrEmail;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
