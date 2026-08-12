package com.abrar.BOOKSTORE.Login.auth;
//We use the AuthenticationManager to authenticate the user based on the provided credentials.

//Upon successful authentication, we generate a JWT token using the JwtTokenProvider.
//The token is then sent back in the response.

import com.abrar.BOOKSTORE.Login.ApiResponse;
import com.abrar.BOOKSTORE.Login.jwt.JwtTokenProvider;
import com.abrar.BOOKSTORE.Login.conf.LoginRequest;
import com.abrar.BOOKSTORE.Login.conf.SignupRequest;
import com.abrar.BOOKSTORE.Login.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    @Getter
    private final UserRepository userRepository;
    @Getter
    private final PasswordEncoder passwordEncoder;
    private final RateLimiter rateLimiter;

    public AuthController(AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            RateLimiter rateLimiter) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.rateLimiter = rateLimiter;
    }

    // Here, SignupRequest and LoginRequest are DTOs (Data Transfer Objects) that
    // represent the data sent in registration and login requests.
    @Autowired
    private AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        authService.registerUser(signupRequest);
        return ResponseEntity.ok(new ApiResponse(true,
                "User registered successfully. Check your email to verify your account before logging in."));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        // Same brute-force protection RateLimiter already gives /forgot-password,
        // applied here too since this JSON endpoint was previously unthrottled.
        // Prefixed so this doesn't share its attempt budget with password-reset,
        // which keys RateLimiter by bare IP.
        if (!rateLimiter.allow("login:" + request.getRemoteAddr())) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponse.error("Too many login attempts. Please try again in a few minutes."));
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtTokenProvider.generateToken(authentication);

        return ResponseEntity.ok(new JwtAuthenticationResponse(token));
    }

}