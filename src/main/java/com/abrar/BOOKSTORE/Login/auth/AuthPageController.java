package com.abrar.BOOKSTORE.Login.auth;

import com.abrar.BOOKSTORE.Login.conf.SignupRequest;
import com.abrar.BOOKSTORE.Login.user.User;
import com.abrar.BOOKSTORE.Login.user.UserRepository;
import com.abrar.BOOKSTORE.service.FileStorageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
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
            model.addAttribute("error", "Please fill in all fields.");
            return "register";
        }
        try {
            authService.registerUser(signupRequest);
        } catch (AuthenticationServiceException ex) {
            // e.g. duplicate username/email - show the error instead of a 500 page
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("signupRequest", signupRequest);
            return "register";
        }
        return "redirect:/login?registered";
    }

    @GetMapping("/profile")
    public String profilePage(Authentication authentication, Model model) {
        User user = userRepository.findByUsernameOrEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));
        model.addAttribute("user", user);
        return "profile";
    }

    @GetMapping("/settings")
    public String settingsPage() {
        return "settings";
    }

    @PostMapping("/profile/avatar")
    public String uploadAvatar(@RequestParam MultipartFile avatar, Authentication authentication,
            RedirectAttributes redirectAttributes) {
        User user = userRepository.findByUsernameOrEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));
        try {
            user.setAvatarUrl(fileStorageService.store(avatar, "avatars"));
            userRepository.save(user);
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/profile";
    }
}