package com.abrar.BOOKSTORE.Login.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    @Async
    public void sendVerificationEmail(String toEmail, String token) {
        String link = baseUrl + "/verify-email?token=" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        if (fromAddress != null && !fromAddress.isBlank()) {
            message.setFrom(fromAddress);
        }
        message.setTo(toEmail);
        message.setSubject("Verify your BookStore email");
        message.setText("Welcome! Please confirm your email address to finish setting up your account.\n\n" +
                "Verify your email here (valid for 24 hours):\n" + link +
                "\n\nIf you didn't create this account, you can ignore this email.");
        try {
            mailSender.send(message);
        } catch (Exception e) {
            // Same reasoning as sendPasswordResetEmail: don't leak SMTP errors to
            // the user, just log so an operator can see delivery is broken.
            log.error("Failed to send verification email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String token) {
        String link = baseUrl + "/reset-password?token=" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        if (fromAddress != null && !fromAddress.isBlank()) {
            message.setFrom(fromAddress);
        }
        message.setTo(toEmail);
        message.setSubject("BookStore password reset");
        message.setText("Someone (hopefully you) requested a password reset.\n\n" +
                "Reset your password here (valid for 30 minutes):\n" + link +
                "\n\nIf you didn't request this, you can ignore this email.");
        try {
            mailSender.send(message);
        } catch (Exception e) {
            // Don't leak SMTP errors to the user - and don't reveal whether the
            // account existed. Log it so an operator can see delivery is broken
            // (e.g. spring.mail.* isn't configured yet).
            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
        }
    }
}