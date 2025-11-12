package com.mealplanner.controller;

import com.mealplanner.model.User;
import com.mealplanner.repository.SavedMealPlanRepository;
import com.mealplanner.service.FoodLogService;
import com.mealplanner.model.FoodLog;
import java.util.List;
import java.util.Map;
import com.mealplanner.repository.UserRepository;
import com.mealplanner.service.UserFoodPreferencesService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final UserRepository userRepository;
    private final SavedMealPlanRepository mealPlanRepository;
    private final FoodLogService foodLogService;
    private final UserFoodPreferencesService preferencesService;

    public DashboardController(UserRepository userRepository, SavedMealPlanRepository mealPlanRepository, FoodLogService foodLogService, UserFoodPreferencesService preferencesService) {
        this.userRepository = userRepository;
        this.mealPlanRepository = mealPlanRepository;
        this.foodLogService = foodLogService;
        this.preferencesService = preferencesService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal OAuth2User principal, Model model, javax.servlet.http.HttpSession session) {
        if (principal == null) return "redirect:/login";
        String email = principal.getAttribute("email");
        String googleId = principal.getAttribute("sub");
        String name = principal.getAttribute("name");
        String picture = principal.getAttribute("picture");
        User user = null;
        if (Boolean.TRUE.equals(session.getAttribute("impersonating"))) {
            Object idObj = session.getAttribute("impersonateUserId");
            if (idObj instanceof Long) {
                user = userRepository.findById((Long) idObj).orElse(null);
            }
        }
        if (user == null) {
            if (email != null) user = userRepository.findByEmail(email).orElse(null);
            if (user == null && googleId != null) user = userRepository.findByGoogleId(googleId).orElse(null);
        }
        if (user == null) {
            // Fallback: create user record if missing to avoid redirect loops
            String em = (email != null) ? email : (googleId != null ? googleId + "@google.local" : "user@google.local");
            String nm = (name != null) ? name : (email != null ? email : "User");
            String gid = (googleId != null) ? googleId : em;
            user = new User(em, nm, gid);
            if (picture != null) user.setProfilePictureUrl(picture);
            userRepository.save(user);
        }
        List<com.mealplanner.model.SavedMealPlan> recent = mealPlanRepository.findTop10ByUserOrderByCreatedAtDesc(user);
        List<FoodLog> todaysFoodLogs = foodLogService.getTodaysFoodLogs(user);
        Map<String, Integer> todaysTotals = new java.util.HashMap<>();
        int tCal = 0, tP = 0, tC = 0, tF = 0;
        for (FoodLog fl : todaysFoodLogs) {
            tCal += fl.getCalories();
            tP += fl.getProtein();
            tC += fl.getCarbs();
            tF += fl.getFat();
        }
        todaysTotals.put("calories", tCal);
        todaysTotals.put("protein", tP);
        todaysTotals.put("carbs", tC);
        todaysTotals.put("fat", tF);
        com.mealplanner.model.SavedMealPlan latestPlan = recent.isEmpty() ? null : recent.get(0);
        // Parse latest plan JSON for Quick Add modal
        com.mealplanner.model.MealPlan latestMealPlan = null;
        if (latestPlan != null && latestPlan.getMealPlanJson() != null) {
            try {
                latestMealPlan = new com.fasterxml.jackson.databind.ObjectMapper().readValue(latestPlan.getMealPlanJson(), com.mealplanner.model.MealPlan.class);
            } catch (Exception ignore) {}
        }

        model.addAttribute("user", user);
        model.addAttribute("savedPlans", recent);
        model.addAttribute("todaysFoodLogs", todaysFoodLogs);
        model.addAttribute("todaysTotals", todaysTotals);
        model.addAttribute("latestPlan", latestPlan);
        model.addAttribute("latestMealPlan", latestMealPlan);
        model.addAttribute("userPreferences", preferencesService.getUserPreferences(user));
        model.addAttribute("impersonating", Boolean.TRUE.equals(session.getAttribute("impersonating")));
        return "dashboard";
    }
}
