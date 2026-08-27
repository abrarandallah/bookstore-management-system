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
        // Username collisions are shown to the user directly - picking an
        // already-taken handle is normal signup friction, not a privacy leak.
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new AuthenticationServiceException("Username is already in use.");
        }

        // Email collisions are NOT shown to the user - unlike a username, an
        // email address is tied to a real identity, so confirming "this
        // email is already registered" would let an attacker enumerate
        // which addresses have accounts here. Instead this silently no-ops
        // (no new user, no verification email) and notifies the *existing*
        // account's inbox instead - the only place someone who actually
        // owns that address will see it. Either way the caller (see
        // AuthPageController) shows the same generic
        // "check your email" success message.
        userRepository.findByEmail(signupRequest.getEmail()).ifPresentOrElse(
                existing -> emailService.sendAccountAlreadyExistsEmail(existing.getEmail()),
                () -> createUser(signupRequest));
    }

    private void createUser(SignupRequest signupRequest) {
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