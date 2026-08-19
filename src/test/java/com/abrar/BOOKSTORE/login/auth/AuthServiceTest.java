package com.abrar.BOOKSTORE.login.auth;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.abrar.BOOKSTORE.Login.auth.AuthService;
import com.abrar.BOOKSTORE.Login.auth.EmailService;
import com.abrar.BOOKSTORE.Login.conf.SignupRequest;
import com.abrar.BOOKSTORE.Login.user.EmailVerificationToken;
import com.abrar.BOOKSTORE.Login.user.EmailVerificationTokenRepository;
import com.abrar.BOOKSTORE.Login.user.User;
import com.abrar.BOOKSTORE.Login.user.UserRepository;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Covers the enumeration-safe registration flow specifically - see the
 * comments in {@link AuthService#registerUser} for the reasoning. A
 * duplicate *username* should still fail loudly (that's normal signup UX);
 * a duplicate *email* must NOT be distinguishable from a fresh signup by
 * anything this method does or throws.
 */
@ContextConfiguration(classes = { AuthService.class })
@ExtendWith(SpringExtension.class)
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private EmailVerificationTokenRepository verificationTokenRepository;

    @MockBean
    private EmailService emailService;

    private final SignupRequest signupRequest = new SignupRequest("newreader", "newreader@example.com",
            "password123");

    @Test
    void testRegisterUserRejectsADuplicateUsername() {
        when(userRepository.existsByUsername("newreader")).thenReturn(true);

        AuthenticationServiceException ex = assertThrows(AuthenticationServiceException.class,
                () -> authService.registerUser(signupRequest));

        // Deliberately says "Username", not the old combined
        // "Username or email" message - see AuthService.
        org.junit.jupiter.api.Assertions.assertEquals("Username is already in use.", ex.getMessage());
        verify(userRepository, never()).findByEmail(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testRegisterUserWithFreeUsernameAndEmailCreatesTheAccount() {
        when(userRepository.existsByUsername("newreader")).thenReturn(false);
        when(userRepository.findByEmail("newreader@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(verificationTokenRepository.save(any(EmailVerificationToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        authService.registerUser(signupRequest);

        verify(userRepository).save(any(User.class));
        verify(verificationTokenRepository).save(any(EmailVerificationToken.class));
        verify(emailService).sendVerificationEmail(eq("newreader@example.com"), anyString());
        verify(emailService, never()).sendAccountAlreadyExistsEmail(anyString());
    }

    /**
     * The core enumeration-safety guarantee: when the email is already
     * registered, no new user row is created, no verification email is
     * sent to the submitter, and nothing is thrown back to the caller -
     * the method just returns normally, exactly as it would for a brand
     * new signup. Only the existing account's own inbox is notified.
     */
    @Test
    void testRegisterUserWithAnAlreadyRegisteredEmailDoesNotCreateADuplicateOrThrow() {
        User existingOwner = new User("someoneElse", "newreader@example.com", "hash", "ROLE_USER");
        when(userRepository.existsByUsername("newreader")).thenReturn(false);
        when(userRepository.findByEmail("newreader@example.com")).thenReturn(Optional.of(existingOwner));
        doNothing().when(emailService).sendAccountAlreadyExistsEmail("newreader@example.com");

        authService.registerUser(signupRequest);

        verify(userRepository, never()).save(any());
        verify(verificationTokenRepository, never()).save(any());
        verify(emailService, never()).sendVerificationEmail(anyString(), anyString());
        verify(emailService).sendAccountAlreadyExistsEmail("newreader@example.com");
    }
}