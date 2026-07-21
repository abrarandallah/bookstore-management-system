package com.abrar.BOOKSTORE.Login.auth;

import com.abrar.BOOKSTORE.Login.jwt.JwtTokenProvider;
import com.abrar.BOOKSTORE.Login.conf.LoginRequest;
import com.abrar.BOOKSTORE.Login.conf.SignupRequest;
import com.abrar.BOOKSTORE.Login.user.User;
import com.abrar.BOOKSTORE.Login.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


// handle user registration and authentication
@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    public void registerUser(SignupRequest signupRequest) {
        // Check if the username is already taken
        if (userRepository.existsByUsernameOrEmail(signupRequest.getUsername(), signupRequest.getEmail())) {
            throw new AuthenticationServiceException("Username or email is already in use.");
        }

        // Create a new user object
        User user = new User(signupRequest.getUsername(), signupRequest.getEmail(),
                passwordEncoder.encode(signupRequest.getPassword()), "ROLE_USER");

        // Save the user to the database
        userRepository.save(user);
    }

    public String authenticateUser(LoginRequest loginRequest) {
        // Authenticate the user using Spring Security's AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );

        // Generate a JWT token
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return tokenProvider.generateToken(authentication);
    }
}



