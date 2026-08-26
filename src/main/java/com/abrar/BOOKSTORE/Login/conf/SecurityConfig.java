package com.abrar.BOOKSTORE.Login.conf;
// We configure security settings, including password encoding and

// authorization rules. Every route requires a logged-in session except the
// handful explicitly listed as public below.

import com.abrar.BOOKSTORE.Login.auth.LoginRateLimitFilter;
import com.abrar.BOOKSTORE.Login.auth.RateLimiter;
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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

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
    public LoginRateLimitFilter loginRateLimitFilter() {
        return new LoginRateLimitFilter(rateLimiter);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Without this, browsers send the full page URL - including anything in
                // the query string, like the password-reset token - as a Referer header
                // to every external resource the page loads (our Bootstrap/FontAwesome
                // CDNs). no-referrer strips that entirely, for every page on the site.
                .headers(headers -> headers
                        .referrerPolicy(
                                referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
                        // Restricts where the browser will load scripts/styles/images/fonts
                        // from, matched against what fragments/layout.html and the page
                        // templates actually load:
                        // - Bootstrap CSS+JS from jsdelivr
                        // - Google Fonts stylesheet (fonts.googleapis.com) + the actual
                        // font files it points to (fonts.gstatic.com)
                        // - FontAwesome from cdnjs
                        // - 'unsafe-inline' on script-src because several pages
                        // (layout.html's theme toggle, settings.html, bookRead.html,
                        // readingHistory.html, bookFinished.html, bookShare.html) use
                        // inline <script> blocks rather than external files - tightening
                        // this further would mean moving all of those to external files
                        // and/or a nonce-based CSP, which is a bigger follow-up, not a
                        // one-line header change.
                        // object-src/base-uri/form-action are locked down since nothing in
                        // this app needs plugins, a non-default <base>, or cross-origin
                        // form posts.
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; "
                                        + "script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; "
                                        + "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net "
                                        + "https://fonts.googleapis.com https://cdnjs.cloudflare.com; "
                                        + "font-src 'self' https://fonts.gstatic.com https://cdnjs.cloudflare.com; "
                                        + "img-src 'self' data:; "
                                        + "object-src 'none'; "
                                        + "base-uri 'self'; "
                                        + "form-action 'self'")))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/about", "/login", "/register", "/forgot-password", "/reset-password",
                                "/verify-email", "/resend-verification",
                                // Container/orchestrator healthchecks (see docker-compose.yml)
                                // hit this directly, with no session or bearer token.
                                "/actuator/health",
                                // Search-engine crawlers hit these anonymously too - see
                                // SitemapController.
                                "/robots.txt", "/sitemap.xml",
                                // Legal pages - same reasoning as /about.
                                "/privacy", "/terms",
                                // Browsing and reading are open to everyone - only the
                                // personal actions tied to an account (saving reading
                                // progress, marking finished, "My Books", reviews, reading
                                // history) stay behind the anyRequest().authenticated()
                                // catch-all below. See BookController#readBook, which
                                // already handles a null Principal for this route.
                                "/available_books", "/available_books/results", "/available_books/*/read",
                                "/random_book", "/genres",
                                // The share page is meant to be opened by anyone the link is sent
                                // to, logged in or not - that's the whole point of a share link.
                                "/available_books/*/share",
                                "/css/**", "/js/**", "/images/**", "/img/**", "/uploads/**")
                        .permitAll()
                        .anyRequest().authenticated())
                // No custom exceptionHandling() here - with nothing else competing
                // for the default AuthenticationEntryPoint (this app now has exactly
                // one auth mechanism: session-cookie form login), formLogin() below
                // registers its own correct LoginUrlAuthenticationEntryPoint("/login")
                // automatically. A previous version of this app also had a
                // JWT-authenticated JSON API under /api/**, which needed a second,
                // JSON-appropriate entry point split out here - see git history if
                // that's ever reintroduced, and note the caveat that motivated the
                // two-mapping approach:
                // https://github.com/spring-projects/spring-security/issues/13787.
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