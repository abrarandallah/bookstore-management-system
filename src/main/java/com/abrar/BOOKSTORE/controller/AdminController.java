package com.abrar.BOOKSTORE.controller;

import com.abrar.BOOKSTORE.Login.user.User;
import com.abrar.BOOKSTORE.Login.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('LIBRARIAN')")
public class AdminController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "admin/users";
    }

    private static final java.util.Set<String> VALID_ROLES = java.util.Set.of("ROLE_USER", "ROLE_LIBRARIAN");

    @PostMapping("/users/{id}/role")
    public String changeRole(@PathVariable long id, @RequestParam String role,
            Authentication authentication, Model model) {
        if (!VALID_ROLES.contains(role)) {
            model.addAttribute("error", "Invalid role.");
            model.addAttribute("users", userRepository.findAll());
            return "admin/users";
        }
        User target = userRepository.findById(id).orElse(null);
        if (target == null) {
            return "redirect:/admin/users";
        }
        boolean demotingLastLibrarian = "ROLE_LIBRARIAN".equals(target.getRole())
                && !"ROLE_LIBRARIAN".equals(role)
                && userRepository.countByRole("ROLE_LIBRARIAN") <= 1;
        if (demotingLastLibrarian) {
            model.addAttribute("error", "Can't remove the last librarian account.");
            model.addAttribute("users", userRepository.findAll());
            return "admin/users";
        }
        target.setRole(role);
        userRepository.save(target);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/reset-password")
    public String resetPassword(@PathVariable long id, @RequestParam String newPassword, Model model) {
        User target = userRepository.findById(id).orElse(null);
        if (target == null) {
            return "redirect:/admin/users";
        }
        if (newPassword == null || newPassword.length() < 8) {
            model.addAttribute("error", "Password must be at least 8 characters.");
            model.addAttribute("users", userRepository.findAll());
            return "admin/users";
        }
        target.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(target);
        model.addAttribute("message", "Password updated for " + target.getUsernameOrEmail() + ".");
        model.addAttribute("users", userRepository.findAll());
        return "admin/users";
    }
}