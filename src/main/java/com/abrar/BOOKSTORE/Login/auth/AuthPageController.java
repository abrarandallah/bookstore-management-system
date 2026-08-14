package com.abrar.BOOKSTORE.Login.auth;

import com.abrar.BOOKSTORE.Login.conf.SignupRequest;
import com.abrar.BOOKSTORE.Login.user.User;
import com.abrar.BOOKSTORE.Login.user.UserRepository;
import com.abrar.BOOKSTORE.service.AccountService;
import com.abrar.BOOKSTORE.service.AchievementService;
import com.abrar.BOOKSTORE.service.FileStorageService;
import com.abrar.BOOKSTORE.service.ReadingProgressService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthPageController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private AchievementService achievementService;

    @Autowired
    private ReadingProgressService readingProgressService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AccountService accountService;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        if (!model.containsAttribute("signupRequest")) {
            model.addAttribute("signupRequest", new SignupRequest());
        }
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute SignupRequest signupRequest, BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            // Show the actual validation message (e.g. "Password must be at
            // least 8 characters long.") instead of a generic one, so the
            // user knows what to fix instead of just seeing "fill in all
            // fields" - the fields may well already be filled in with an
            // invalid value.
            String error = bindingResult.getFieldErrors().stream()
                    .findFirst()
                    .map(fieldError -> fieldError.getDefaultMessage())
                    .orElse("Please fill in all fields.");
            model.addAttribute("error", error);
            return "register";
        }
        try {
            authService.registerUser(signupRequest);
        } catch (AuthenticationServiceException ex) {
            // e.g. duplicate username/email - show the error instead of a 500 page
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("signupRequest", signupRequest);
            return "register";
        } catch (DataIntegrityViolationException ex) {
            // Safety net for the DB unique constraint on email/usernameOrEmail:
            // existsByUsernameOrEmail() above already checks for this, but two
            // signups for the same email arriving at nearly the same instant
            // could both pass that check before either finishes saving. Same
            // user-facing message as the expected case above.
            model.addAttribute("error", "Username or email is already in use.");
            model.addAttribute("signupRequest", signupRequest);
            return "register";
        }
        return "redirect:/login?registered";
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByUsernameOrEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));
    }

    @GetMapping("/profile")
    public String profilePage(Authentication authentication, Model model) {
        User user = currentUser(authentication);
        model.addAttribute("user", user);
        model.addAttribute("achievements", achievementService.getEarnedAchievements(user));
        model.addAttribute("finishedBooksCount", readingProgressService.countFinished(user));
        return "profile";
    }

    @GetMapping("/settings")
    public String settingsPage() {
        return "settings";
    }

    @PostMapping("/profile/avatar")
    public String uploadAvatar(@RequestParam MultipartFile avatar, Authentication authentication,
            RedirectAttributes redirectAttributes) {
        User user = currentUser(authentication);
        try {
            user.setAvatarUrl(fileStorageService.store(avatar, "avatars"));
            userRepository.save(user);
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/profile";
    }

    // In-app password change for an already-authenticated user - distinct
    // from the /forgot-password + /reset-password flow, which is for users
    // who are logged OUT and need an email link. Since the session already
    // proves who they are, this skips the email step entirely and instead
    // confirms identity by asking for the current password.
    @GetMapping("/change-password")
    public String changePasswordForm() {
        return "changePassword";
    }

    @PostMapping("/change-password")
    public String changePasswordSubmit(@RequestParam String currentPassword, @RequestParam String password,
            @RequestParam String confirmPassword, Authentication authentication, Model model) {
        User user = currentUser(authentication);
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            model.addAttribute("error", "Current password is incorrect.");
            return "changePassword";
        }
        if (password == null || password.length() < 8) {
            model.addAttribute("error", "New password must be at least 8 characters.");
            return "changePassword";
        }
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "New passwords don't match.");
            return "changePassword";
        }
        if (passwordEncoder.matches(password, user.getPassword())) {
            model.addAttribute("error", "New password must be different from your current password.");
            return "changePassword";
        }
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        model.addAttribute("message", "Your password has been changed.");
        return "changePassword";
    }

    @GetMapping("/delete-account")
    public String deleteAccountForm() {
        return "deleteAccount";
    }

    // Requires re-entering the current password, same as change-password
    // above - the session already proves who's logged in, but a
    // permanent, cascading delete warrants an extra identity check beyond
    // "whoever currently holds this browser session".
    @PostMapping("/delete-account")
    public String deleteAccountSubmit(@RequestParam String currentPassword, Authentication authentication,
            HttpServletRequest request, Model model) {
        User user = currentUser(authentication);
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            model.addAttribute("error", "Current password is incorrect.");
            return "deleteAccount";
        }
        try {
            accountService.deleteAccount(user);
        } catch (IllegalStateException ex) {
            model.addAttribute("error", ex.getMessage());
            return "deleteAccount";
        }
        // The account (and the session's backing DB row) no longer exists -
        // invalidate the session and clear the security context directly
        // rather than redirecting to /logout, so there's no window where a
        // now-stale session is still considered authenticated.
        request.getSession().invalidate();
        SecurityContextHolder.clearContext();
        return "redirect:/login?accountDeleted";
    }
}