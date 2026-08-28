package com.abrar.BOOKSTORE.login.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.abrar.BOOKSTORE.Login.user.CustomUserDetailsService;
import com.abrar.BOOKSTORE.Login.user.User;
import com.abrar.BOOKSTORE.Login.user.UserRepository;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

// Regression coverage for a real bug: the login form's field is labeled
// "Username or Email", but loadUserByUsername() used to only query the
// usernameOrEmail column, so someone who typed their email address at
// login (e.g. right after a librarian reset their password) would get
// "user not found" even though their account existed. Fixed by querying
// both columns via UserRepository.findByUsernameOrEmailOrEmail(); these
// tests exist so that fix can't silently regress.
@ContextConfiguration(classes = { CustomUserDetailsService.class })
@ExtendWith(SpringExtension.class)
class CustomUserDetailsServiceTest {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private UserRepository userRepository;

    @Test
    void testLoadUserByUsernameFindsUserByUsername() {
        User user = new User("reader", "reader@example.com", "hash", "ROLE_USER");
        when(userRepository.findByUsernameOrEmailOrEmail("reader")).thenReturn(Optional.of(user));

        UserDetails result = customUserDetailsService.loadUserByUsername("reader");

        assertEquals("reader", result.getUsername());
    }

    @Test
    void testLoadUserByUsernameFindsUserByEmailEvenThoughFieldIsCalledUsername() {
        // The actual bug: someone types their email into the "Username or
        // Email" field. usernameOrEmail on the User row is "reader", but
        // they typed "reader@example.com" - it must still resolve.
        User user = new User("reader", "reader@example.com", "hash", "ROLE_USER");
        when(userRepository.findByUsernameOrEmailOrEmail("reader@example.com"))
                .thenReturn(Optional.of(user));

        UserDetails result = customUserDetailsService.loadUserByUsername("reader@example.com");

        assertEquals("reader", result.getUsername());
    }

    @Test
    void testLoadUserByUsernameThrowsWhenNeitherColumnMatches() {
        when(userRepository.findByUsernameOrEmailOrEmail("nobody")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("nobody"));
    }
}