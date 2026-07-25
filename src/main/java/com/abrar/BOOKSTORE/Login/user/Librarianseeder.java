package com.abrar.BOOKSTORE.Login.user;

public class Librarianseeder {
    
}
package com.abrar.BOOKSTORE.Login.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

// Role/RoleRepository still aren't wired into the auth flow (see User.getRole()),
// so this seeds the one librarian account via the same simple `role` string the
// rest of the app uses. Replace with a real admin flow if/when one exists.
@Component
public class LibrarianSeeder implements CommandLineRunner {

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
    }
}