package com.abrar.BOOKSTORE.Login.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

// Seeds the one librarian account via the simple `role` string field on
// User. There's now a real admin panel (/admin/users) for promoting other
// users afterward - this just handles the bootstrap problem of getting the
// very first librarian in.
@Component
public class LibrarianSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(LibrarianSeeder.class);
    private static final String DEFAULT_PASSWORD = "change-this-password";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.librarian.username}")
    private String librarianUsername;

    @Value("${app.librarian.email}")
    private String librarianEmail;

    @Value("${app.librarian.password}")
    private String librarianPassword;

    @Override
    public void run(String... args) {
        boolean librarianExists = userRepository.findAll().stream()
                .anyMatch(u -> "ROLE_LIBRARIAN".equals(u.getRole()));
        if (librarianExists) {
            return;
        }
        List<User> existing = userRepository.findAll();
        boolean usernameTaken = existing.stream()
                .anyMatch(u -> librarianUsername.equals(u.getUsernameOrEmail()));
        if (usernameTaken) {
            return;
        }
        User librarian = new User(librarianUsername, librarianEmail,
                passwordEncoder.encode(librarianPassword), "ROLE_LIBRARIAN");
        userRepository.save(librarian);
        if (DEFAULT_PASSWORD.equals(librarianPassword)) {
            log.warn("The librarian account '{}' was created with the DEFAULT password. "
                    + "Set LIBRARIAN_PASSWORD in your .env file before this site is reachable from the internet.",
                    librarianUsername);
        }
    }
}