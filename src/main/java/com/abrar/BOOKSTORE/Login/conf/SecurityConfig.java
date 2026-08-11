package com.abrar.BOOKSTORE.Login.conf;
// We configure security settings, including password encoding and

// authorization rules. Every route requires a logged-in session except the
// handful explicitly listed as public below.
//
// Two coexisting auth mechanisms: the browser UI uses session-cookie auth
// via formLogin (everything below), and a separate JSON API under
// /api/auth/** authenticates via a JWT bearer token instead (see
// JwtAuthorizationFilter). The JWT filter is a no-op passthrough when no
// bearer token is present, so it doesn't interfere with session-based
// requests - the two simply don't overlap in practice.

import com.abrar.BOOKSTORE.Login.auth.JwtAuthenticationEntryPoint;
import com.abrar.BOOKSTORE.Login.auth.LoginRateLimitFilter;
import com.abrar.BOOKSTORE.Login.auth.RateLimiter;
import com.abrar.BOOKSTORE.Login.jwt.JwtAuthorizationFilter;
import com.abrar.BOOKSTORE.Login.jwt.JwtTokenProvider;
import com.abrar.BOOKSTORE.Login.user.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private RateLimiter rateLimiter;

    @Autowired
    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(daoAuthenticationProvider());
    }

    @Bean
    public JwtAuthorizationFilter jwtAuthorizationFilter() {
        return new JwtAuthorizationFilter(jwtTokenProvider, userDetailsService);
    }

    @Bean
    public LoginRateLimitFilter loginRateLimitFilter() {
        return new LoginRateLimitFilter(rateLimiter);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {
                })
                // /api/** authenticates via a Bearer token read from the Authorization
                // header - never a cookie - so it isn't vulnerable to CSRF and is
                // exempted here. Every other route uses session-cookie auth via
                // formLogin and needs CSRF protection, since that's what a forged
                // cross-site request would ride on.
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
                // Without this, browsers send the full page URL - including anything in
                // the query string, like the password-reset token - as a Referer header
                // to every external resource the page loads (our Bootstrap/FontAwesome
                // CDNs). no-referrer strips that entirely, for every page on the site.
                .headers(headers -> headers.referrerPolicy(
                        referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER)))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/about", "/login", "/register", "/forgot-password", "/reset-password",
                                "/verify-email", "/resend-verification",
                                "/api/auth/**",
                                // The share page is meant to be opened by anyone the link is sent
                                // to, logged in or not - that's the whole point of a share link.
                                "/available_books/*/share",
                                "/css/**", "/js/**", "/images/**", "/img/**", "/uploads/**", "/*.jpg", "/*.png")
                        .permitAll()
                        .anyRequest().authenticated())
                // Scoped to /api/** only - anything else falls through to formLogin's
                // own default entry point (redirect to /login), which is what an
                // expired browser session needs. A blanket override here would
                // replace that redirect with a bare 401 for the whole site.
                .exceptionHandling(ex -> ex.defaultAuthenticationEntryPointFor(
                        new JwtAuthenticationEntryPoint(), new AntPathRequestMatcher("/api/**")))
                .addFilterBefore(jwtAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(loginRateLimitFilter(), UsernamePasswordAuthenticationFilter.class)
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("usernameOrEmail")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/available_books", true)
                        // Default failure behavior redirects to /login?error for every
                        // AuthenticationException, which would tell an unverified user
                        // their password was wrong. Distinguish that case so login.html
                        // can point them at /resend-verification instead.
                        .failureHandler((request, response, exception) -> {
                            String redirectParam = exception instanceof DisabledException ? "unverified" : "error";
                            response.sendRedirect(request.getContextPath() + "/login?" + redirectParam);
                        })
                        .permitAll())
                .logout(logout -> logout.permitAll());

        return http.build();
    }
}