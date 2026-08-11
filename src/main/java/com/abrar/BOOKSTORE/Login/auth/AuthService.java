package com.abrar.BOOKSTORE.Login.auth;

import com.abrar.BOOKSTORE.Login.conf.SignupRequest;
import com.abrar.BOOKSTORE.Login.user.EmailVerificationToken;
import com.abrar.BOOKSTORE.Login.user.EmailVerificationTokenRepository;
import com.abrar.BOOKSTORE.Login.user.User;
import com.abrar.BOOKSTORE.Login.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

// handle user registration
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailVerificationTokenRepository verificationTokenRepository;

    @Autowired
    private EmailService emailService;

    public void registerUser(SignupRequest signupRequest) {
        // Check if the username is already taken
        if (userRepository.existsByUsernameOrEmail(signupRequest.getUsername(), signupRequest.getEmail())) {
            throw new AuthenticationServiceException("Username or email is already in use.");
        }

        // Create a new user object. New signups start unverified - they can't
        // log in (see UserPrincipal.isEnabled()) until they click the link
        // sent below. Existing users predating this feature are unaffected:
        // they were backfilled as verified when the column was added (see
        // User.verified).
        User user = new User(signupRequest.getUsername(), signupRequest.getEmail(),
                passwordEncoder.encode(signupRequest.getPassword()), "ROLE_USER");
        user.setVerified(false);

        // Save the user to the database
        userRepository.save(user);

        EmailVerificationToken verificationToken = new EmailVerificationToken(UUID.randomUUID().toString(), user);
        verificationTokenRepository.save(verificationToken);
        emailService.sendVerificationEmail(user.getEmail(), verificationToken.getToken());
    }
}