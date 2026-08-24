package com.abrar.BOOKSTORE.controller;

import com.abrar.BOOKSTORE.Login.user.User;
import com.abrar.BOOKSTORE.Login.user.UserRepository;
import com.abrar.BOOKSTORE.service.AccountService;
import com.abrar.BOOKSTORE.service.PagedResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('LIBRARIAN')")
public class AdminController {

    private static final int PAGE_SIZE = 20;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AccountService accountService;

    @GetMapping("/users")
    public String listUsers(@RequestParam(required = false, defaultValue = "1") int page, Model model) {
        int pageIndex = Math.max(page, 1) - 1;
        Page<User> result = userRepository.findAll(
                PageRequest.of(pageIndex, PAGE_SIZE, Sort.by("usernameOrEmail").ascending()));
        model.addAttribute("users", result.getContent());
        model.addAttribute("pagination",
                new PagedResult<>(result.getContent(), Math.max(page, 1), PAGE_SIZE, result.getTotalElements()));
        // The stat cards need whole-system counts, not just this page's -
        // see admin/users.html, which used to derive them straight from
        // the (now-paginated) users list.
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("librarianCount", userRepository.countByRole("ROLE_LIBRARIAN"));
        model.addAttribute("readerCount", userRepository.countByRole("ROLE_USER"));
        return "admin/users";
    }

    private static final java.util.Set<String> VALID_ROLES = java.util.Set.of("ROLE_USER", "ROLE_LIBRARIAN");

    @PostMapping("/users/{id}/role")
    public String changeRole(@PathVariable long id, @RequestParam String role,
            Authentication authentication, RedirectAttributes redirectAttributes) {
        if (!VALID_ROLES.contains(role)) {
            redirectAttributes.addFlashAttribute("error", "Invalid role.");
            return "redirect:/admin/users";
        }
        User target = userRepository.findById(id).orElse(null);
        if (target == null) {
            return "redirect:/admin/users";
        }
        boolean demotingLastLibrarian = "ROLE_LIBRARIAN".equals(target.getRole())
                && !"ROLE_LIBRARIAN".equals(role)
                && userRepository.countByRole("ROLE_LIBRARIAN") <= 1;
        if (demotingLastLibrarian) {
            redirectAttributes.addFlashAttribute("error", "Can't remove the last librarian account.");
            return "redirect:/admin/users";
        }
        target.setRole(role);
        userRepository.save(target);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/reset-password")
    public String resetPassword(@PathVariable long id, @RequestParam String newPassword,
            RedirectAttributes redirectAttributes) {
        User target = userRepository.findById(id).orElse(null);
        if (target == null) {
            return "redirect:/admin/users";
        }
        if (newPassword == null || newPassword.length() < 8) {
            redirectAttributes.addFlashAttribute("error", "Password must be at least 8 characters.");
            return "redirect:/admin/users";
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
        redirectAttributes.addFlashAttribute("message", message);
        return "redirect:/admin/users";
    }

    // Deleting your own account through this admin route is blocked -
    // self-deletion already has its own dedicated flow (/delete-account)
    // that re-confirms the current password before a permanent, cascading
    // delete. Allowing it here too would let an admin remove themselves
    // with a single click and no such confirmation.
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable long id, Authentication authentication,
            RedirectAttributes redirectAttributes) {
        User target = userRepository.findById(id).orElse(null);
        if (target == null) {
            return "redirect:/admin/users";
        }
        User currentUser = userRepository.findByUsernameOrEmail(authentication.getName()).orElse(null);
        if (currentUser != null && currentUser.getId() == target.getId()) {
            redirectAttributes.addFlashAttribute("error",
                    "You can't delete your own account here - use Delete Account in Settings instead.");
            return "redirect:/admin/users";
        }
        try {
            accountService.deleteAccount(target);
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/users";
        }
        redirectAttributes.addFlashAttribute("message", "Deleted " + target.getUsernameOrEmail() + ".");
        return "redirect:/admin/users";
    }
}