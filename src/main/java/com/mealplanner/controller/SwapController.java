package com.mealplanner.controller;

import com.mealplanner.model.User;
import com.mealplanner.repository.UserRepository;
import com.mealplanner.service.SwapService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/swap")
public class SwapController {

    private final SwapService swapService;
    private final UserRepository userRepository;

    public SwapController(SwapService swapService, UserRepository userRepository) {
        this.swapService = swapService;
        this.userRepository = userRepository;
    }

    @PostMapping("/ingredient-options")
    @ResponseBody
    public org.springframework.http.ResponseEntity<Map<String, Object>> getIngredientSwapOptions(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestParam String originalFood,
            @RequestParam int calories,
            @RequestParam int protein,
            @RequestParam int carbs,
            @RequestParam int fat) {
        Map<String, Integer> macros = Map.of(
                "calories", calories,
                "protein", protein,
                "carbs", carbs,
                "fat", fat
        );
        List<Map<String, Object>> suggestions = swapService.getSwapSuggestions(originalFood, macros);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("suggestions", suggestions);
        return org.springframework.http.ResponseEntity.ok(response);
    }

    @PostMapping("/record")
    @ResponseBody
    public org.springframework.http.ResponseEntity<Map<String, Object>> recordSwap(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestParam String swapType,
            @RequestParam String originalFood,
            @RequestParam String replacementFood,
            @RequestParam String mealContext) {
        if (principal == null) return org.springframework.http.ResponseEntity.status(401).body(Map.of("success", false));
        String email = principal.getAttribute("email");
        String googleId = principal.getAttribute("sub");
        User user = null;
        if (email != null) user = userRepository.findByEmail(email).orElse(null);
        if (user == null && googleId != null) user = userRepository.findByGoogleId(googleId).orElse(null);
        if (user == null) return org.springframework.http.ResponseEntity.status(401).body(Map.of("success", false));

        swapService.recordSwap(user, swapType, originalFood, replacementFood, mealContext);
        return org.springframework.http.ResponseEntity.ok(Map.of("success", true, "message", "Swap recorded"));
    }
}

