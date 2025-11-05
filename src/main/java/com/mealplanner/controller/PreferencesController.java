package com.mealplanner.controller;

import com.mealplanner.model.User;
import com.mealplanner.repository.UserRepository;
import com.mealplanner.service.UserFoodPreferencesService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/preferences")
public class PreferencesController {

    private final UserFoodPreferencesService preferencesService;
    private final UserRepository userRepository;

    public PreferencesController(UserFoodPreferencesService preferencesService,
                                 UserRepository userRepository) {
        this.preferencesService = preferencesService;
        this.userRepository = userRepository;
    }

    @PostMapping("/save")
    public String savePreferences(@AuthenticationPrincipal OAuth2User principal,
                                  @RequestParam String preferredFoods,
                                  @RequestParam(required = false) String avoidedFoods,
                                  @RequestParam String cookingPreference,
                                  @RequestParam String dietaryStyle,
                                  RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        String email = principal.getAttribute("email");
        String googleId = principal.getAttribute("sub");
        User user = null;
        if (email != null) user = userRepository.findByEmail(email).orElse(null);
        if (user == null && googleId != null) user = userRepository.findByGoogleId(googleId).orElse(null);
        if (user == null) return "redirect:/login";

        preferencesService.savePreferences(user, preferredFoods, avoidedFoods, cookingPreference, dietaryStyle);
        redirectAttributes.addFlashAttribute("success", "Food preferences saved!");
        return "redirect:/dashboard";
    }
}

