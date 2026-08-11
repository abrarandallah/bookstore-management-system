package com.abrar.BOOKSTORE.Login.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Caps session-login POSTs per IP, the same protection RateLimiter already
// gives /forgot-password. Runs before UsernamePasswordAuthenticationFilter so
// a rate-limited attempt never reaches the AuthenticationManager at all -
// otherwise a brute-force attempt against /login went completely unthrottled.
// Keyed with a "login:" prefix so this doesn't share its attempt budget with
// the password-reset flow, which keys RateLimiter by bare IP.
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private final RateLimiter rateLimiter;

    public LoginRateLimitFilter(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        if ("POST".equalsIgnoreCase(request.getMethod()) && "/login".equals(request.getServletPath())) {
            if (!rateLimiter.allow("login:" + request.getRemoteAddr())) {
                response.sendRedirect(request.getContextPath() + "/login?rateLimited");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}