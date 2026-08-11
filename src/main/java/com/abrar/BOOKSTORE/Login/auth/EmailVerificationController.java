package com.abrar.BOOKSTORE.Login.auth;

import com.abrar.BOOKSTORE.Login.user.EmailVerificationToken;
import com.abrar.BOOKSTORE.Login.user.EmailVerificationTokenRepository;
import com.abrar.BOOKSTORE.Login.user.User;
import com.abrar.BOOKSTORE.Login.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;
import java.util.UUID;

@Controller
public class EmailVerificationController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailVerificationTokenRepository tokenRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private RateLimiter rateLimiter;

    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam String token, Model model) {
        Optional<EmailVerificationToken> tokenOpt = tokenRepository.findByToken(token);
        if (tokenOpt.isEmpty() || tokenOpt.get().isExpired()) {
            model.addAttribute("error", "This verification link is invalid or has expired. Please request a new one.");
            return "verifyEmailInvalid";
        }
        EmailVerificationToken verificationToken = tokenOpt.get();
        User user = verificationToken.getUser();
        user.setVerified(true);
        userRepository.save(user);
        tokenRepository.delete(verificationToken);
        return "redirect:/login?verified";
    }

    @GetMapping("/resend-verification")
    public String resendVerificationForm() {
        return "resendVerification";
    }

    @PostMapping("/resend-verification")
    public String resendVerificationSubmit(@RequestParam String usernameOrEmail, HttpServletRequest request,
            Model model) {
        String clientIp = request.getRemoteAddr();
        // Keyed with a "verify:" prefix so this doesn't share its attempt
        // budget with /forgot-password (bare IP) or /login (used with a
        // "login:" prefix) - see LoginRateLimitFilter for that one.
        if (!rateLimiter.allow("verify:" + clientIp)) {
            model.addAttribute("error", "Too many requests. Please try again in a few minutes.");
            return "resendVerification";
        }
        Optional<User> userOpt = userRepository.findByUsernameOrEmailOrEmail(usernameOrEmail);
        if (userOpt.isPresent() && !userOpt.get().isVerified()
                && userOpt.get().getEmail() != null && !userOpt.get().getEmail().isBlank()) {
            User user = userOpt.get();
            // One active token per user: clear any previous one first, same
            // pattern as PasswordResetController.
            tokenRepository.deleteByUser(user);
            EmailVerificationToken verificationToken = new EmailVerificationToken(UUID.randomUUID().toString(), user);
            tokenRepository.save(verificationToken);
            emailService.sendVerificationEmail(user.getEmail(), verificationToken.getToken());
        }
        // Same message regardless of whether the account exists or is already
        // verified, so this can't be used to enumerate accounts or their
        // verification status.
        model.addAttribute("message",
                "If an unverified account with that username or email exists, we've sent a new verification link to it.");
        return "resendVerification";
    }
}