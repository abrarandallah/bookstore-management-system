package com.abrar.BOOKSTORE.Login.conf;
//We configure security settings, including

// password encoding and authorization rules.
// We permit access to the /api/auth/** endpoints
// (which includes the login endpoint) without requiring
// authentication. All other endpoints will require authentication.

import com.abrar.BOOKSTORE.Login.jwt.JwtAuthorizationFilter;
import com.abrar.BOOKSTORE.Login.jwt.JwtTokenProvider;
import com.abrar.BOOKSTORE.Login.auth.JwtAuthenticationEntryPoint;
import com.abrar.BOOKSTORE.Login.user.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
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
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    public SecurityConfig(
            UserDetailsService userDetailsService,
            JwtTokenProvider jwtTokenProvider) {
        this.userDetailsService = userDetailsService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthorizationFilter jwtAuthorizationFilter() {
        return new JwtAuthorizationFilter(jwtTokenProvider, userDetailsService);
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
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {
                })
                .csrf(AbstractHttpConfigurer::disable)
                // Without this, browsers send the full page URL - including anything in
                // the query string, like the password-reset token - as a Referer header
                // to every external resource the page loads (our Bootstrap/FontAwesome
                // CDNs). no-referrer strips that entirely, for every page on the site.
                .headers(headers -> headers.referrerPolicy(
                        referrer -> referrer.policy(
                                org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER)))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/register", "/forgot-password", "/reset-password",
                                "/api/auth/**",
                                "/css/**", "/js/**", "/images/**", "/*.jpg", "/*.png")
                        .permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("usernameOrEmail")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/available_books", true)
                        .permitAll())
                .logout(logout -> logout.permitAll())
                .exceptionHandling(ex -> ex.defaultAuthenticationEntryPointFor(
                        new JwtAuthenticationEntryPoint(),
                        new AntPathRequestMatcher("/api/**")))
                .addFilterBefore(jwtAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}