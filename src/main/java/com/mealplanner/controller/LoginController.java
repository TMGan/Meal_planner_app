package com.mealplanner.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;

@Controller
public class LoginController {

    @GetMapping({"/home"})
    public String home(Authentication authentication) {
        if (authentication != null && (authentication.getPrincipal() instanceof org.springframework.security.oauth2.core.user.OAuth2User)) {
            return "redirect:/dashboard";
        }
        return "home";
    }

    @GetMapping("/login")
    public String login(Authentication authentication) {
        if (authentication != null && (authentication.getPrincipal() instanceof org.springframework.security.oauth2.core.user.OAuth2User)) {
            return "redirect:/dashboard";
        }
        return "login";
    }
}
