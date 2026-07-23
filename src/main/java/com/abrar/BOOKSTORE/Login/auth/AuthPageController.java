package com.abrar.BOOKSTORE.Login.auth;

import com.abrar.BOOKSTORE.Login.conf.SignupRequest;
import com.abrar.BOOKSTORE.Login.user.User;
import com.abrar.BOOKSTORE.Login.user.UserRepository;
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

@Controller
public class AuthPageController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

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
}