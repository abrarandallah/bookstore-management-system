package com.abrar.BOOKSTORE.Login.auth;

import com.abrar.BOOKSTORE.Login.user.PasswordResetToken;
import com.abrar.BOOKSTORE.Login.user.PasswordResetTokenRepository;
import com.abrar.BOOKSTORE.Login.user.User;
import com.abrar.BOOKSTORE.Login.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;
import java.util.UUID;

@Controller
public class PasswordResetController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordResetTokenRepository tokenRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RateLimiter rateLimiter;

    @GetMapping("/forgot-password")
    public String forgotPasswordForm() {
        return "forgotPassword";
    }

    @PostMapping("/forgot-password")
    public String forgotPasswordSubmit(@RequestParam String usernameOrEmail, HttpServletRequest request,
            Model model) {
        String clientIp = request.getRemoteAddr();
        if (!rateLimiter.allow(clientIp)) {
            model.addAttribute("error", "Too many reset attempts. Please try again in a few minutes.");
            return "forgotPassword";
        }
        Optional<User> userOpt = userRepository.findByUsernameOrEmailOrEmail(usernameOrEmail);
        if (userOpt.isEmpty() || userOpt.get().getEmail() == null || userOpt.get().getEmail().isBlank()) {
            model.addAttribute("error", "No account found with that username or email.");
            return "forgotPassword";
        }
        User user = userOpt.get();
        // One active token per user: clear any previous one first.
        tokenRepository.deleteByUser(user);
        PasswordResetToken resetToken = new PasswordResetToken(UUID.randomUUID().toString(), user);
        tokenRepository.save(resetToken);
        emailService.sendPasswordResetEmail(user.getEmail(), resetToken.getToken());
        model.addAttribute("message", "A password reset link has been sent to " + user.getEmail() + ".");
        return "forgotPassword";
    }

    @GetMapping("/reset-password")
    public String resetPasswordForm(@RequestParam String token, Model model) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
        if (tokenOpt.isEmpty() || tokenOpt.get().isExpired()) {
            model.addAttribute("error", "This reset link is invalid or has expired. Please request a new one.");
            return "resetPasswordInvalid";
        }
        model.addAttribute("token", token);
        return "resetPassword";
    }

    @PostMapping("/reset-password")
    public String resetPasswordSubmit(@RequestParam String token, @RequestParam String password,
            @RequestParam String confirmPassword, Model model) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
        if (tokenOpt.isEmpty() || tokenOpt.get().isExpired()) {
            model.addAttribute("error", "This reset link is invalid or has expired. Please request a new one.");
            return "resetPasswordInvalid";
        }
        if (password == null || password.length() < 8) {
            model.addAttribute("error", "Password must be at least 8 characters.");
            model.addAttribute("token", token);
            return "resetPassword";
        }
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords don't match.");
            model.addAttribute("token", token);
            return "resetPassword";
        }
        PasswordResetToken resetToken = tokenOpt.get();
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        tokenRepository.delete(resetToken);
        return "redirect:/login?reset";
    }
}