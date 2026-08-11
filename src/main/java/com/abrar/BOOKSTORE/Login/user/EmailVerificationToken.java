package com.abrar.BOOKSTORE.Login.user;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "email_verification_tokens")
public class EmailVerificationToken {

    // Longer-lived than PasswordResetToken's 30 minutes - verification isn't
    // as security-sensitive as a password reset, and users may not check
    // their inbox right after signing up.
    private static final int EXPIRY_MINUTES = 24 * 60;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Instant expiryDate;

    public EmailVerificationToken() {
    }

    public EmailVerificationToken(String token, User user) {
        this.token = token;
        this.user = user;
        this.expiryDate = Instant.now().plusSeconds(EXPIRY_MINUTES * 60L);
    }

    public long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public User getUser() {
        return user;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiryDate);
    }
}