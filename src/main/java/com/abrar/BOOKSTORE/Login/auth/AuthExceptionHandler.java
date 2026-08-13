package com.abrar.BOOKSTORE.Login.auth;

import com.abrar.BOOKSTORE.Login.ApiResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Centralized error handling for {@link AuthController} only (scoped via
 * assignableTypes so it never interferes with the Thymeleaf/MVC book
 * controllers). Previously:
 * - a failed @Valid on signup/login bubbled up as Spring's generic
 * whitelabel 400 page instead of a useful JSON message,
 * - a duplicate username/email (AuthenticationServiceException) surfaced as
 * an unhandled 500,
 * - bad login credentials (AuthenticationException) also surfaced as a 500
 * instead of a 401.
 */
@RestControllerAdvice(assignableTypes = AuthController.class)
public class AuthExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));
        if (message.isBlank()) {
            message = "Validation failed.";
        }
        return ResponseEntity.badRequest().body(ApiResponse.error(message));
    }

    @ExceptionHandler(AuthenticationServiceException.class)
    public ResponseEntity<ApiResponse> handleAuthenticationServiceException(AuthenticationServiceException ex) {
        // Thrown by AuthService.registerUser() when the username/email is already
        // taken.
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        // Safety net for the same race window described in
        // AuthPageController.registerUser(): two signups for the same email
        // arriving close together could both pass the existsByUsernameOrEmail()
        // check before either finishes saving.
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("Username or email is already in use."));
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiResponse> handleDisabledException(DisabledException ex) {
        // Thrown by the AuthenticationManager when the account exists and the
        // password is correct, but User.verified is still false.
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Please verify your email before logging in."));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse> handleAuthenticationException(AuthenticationException ex) {
        // Thrown by the AuthenticationManager on login for bad/unknown credentials.
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid username or password."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleUnexpectedError(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred. Please try again."));
    }
}