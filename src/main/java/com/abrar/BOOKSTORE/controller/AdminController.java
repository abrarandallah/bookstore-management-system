package com.abrar.BOOKSTORE.controller;

import com.abrar.BOOKSTORE.Login.user.User;
import com.abrar.BOOKSTORE.Login.user.UserRepository;
import com.abrar.BOOKSTORE.service.AccountService;
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
    @Autowired
    private AccountService accountService;

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
        // A librarian manually resetting someone's password is already
        // vouching for their identity out-of-band. Without this, an
        // account that was still sitting unverified (e.g. they never
        // clicked the email link) would look like the reset succeeded
        // here, but the user still couldn't log in with the new password -
        // blocked by the separate "please verify your email" check instead.
        boolean wasUnverified = !target.isVerified();
        target.setVerified(true);
        userRepository.save(target);
        String message = "Password updated for " + target.getUsernameOrEmail() + ".";
        if (wasUnverified) {
            message += " Their account was also unverified, so it's now verified too - otherwise they still "
                    + "wouldn't have been able to log in with the new password.";
        }
        model.addAttribute("message", message);
        model.addAttribute("users", userRepository.findAll());
        return "admin/users";
    }

    // Deleting your own account through this admin route is blocked -
    // self-deletion already has its own dedicated flow (/delete-account)
    // that re-confirms the current password before a permanent, cascading
    // delete. Allowing it here too would let an admin remove themselves
    // with a single click and no such confirmation.
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable long id, Authentication authentication, Model model) {
        User target = userRepository.findById(id).orElse(null);
        if (target == null) {
            return "redirect:/admin/users";
        }
        User currentUser = userRepository.findByUsernameOrEmail(authentication.getName()).orElse(null);
        if (currentUser != null && currentUser.getId() == target.getId()) {
            model.addAttribute("error",
                    "You can't delete your own account here - use Delete Account in Settings instead.");
            model.addAttribute("users", userRepository.findAll());
            return "admin/users";
        }
        try {
            accountService.deleteAccount(target);
        } catch (IllegalStateException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("users", userRepository.findAll());
            return "admin/users";
        }
        model.addAttribute("message", "Deleted " + target.getUsernameOrEmail() + ".");
        model.addAttribute("users", userRepository.findAll());
        return "admin/users";
    }
}