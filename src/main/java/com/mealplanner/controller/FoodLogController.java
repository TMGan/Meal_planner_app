package com.mealplanner.controller;

import com.mealplanner.model.FoodLog;
import com.mealplanner.model.User;
import com.mealplanner.repository.UserRepository;
import com.mealplanner.service.FoodLogService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/food-log")
public class FoodLogController {

    private final FoodLogService foodLogService;
    private final UserRepository userRepository;

    public FoodLogController(FoodLogService foodLogService, UserRepository userRepository) {
        this.foodLogService = foodLogService;
        this.userRepository = userRepository;
    }

    @PostMapping("/add")
    public String addFoodLog(@AuthenticationPrincipal OAuth2User principal,
                             @RequestParam String mealName,
                             @RequestParam String foodDescription,
                             @RequestParam int calories,
                             @RequestParam int protein,
                             @RequestParam int carbs,
                             @RequestParam int fat,
                             @RequestParam(required = false, name = "timezone") String timezone,
                             RedirectAttributes redirectAttributes) {
        User user = resolveUser(principal);
        FoodLog foodLog = new FoodLog();
        foodLog.setUser(user);
        foodLog.setMealName(mealName);
        foodLog.setFoodDescription(foodDescription);
        foodLog.setCalories(calories);
        foodLog.setProtein(protein);
        foodLog.setCarbs(carbs);
        foodLog.setFat(fat);
        foodLog.setFromMealPlan(false);
        try {
            java.time.ZoneId zone = (timezone != null && !timezone.isBlank()) ? java.time.ZoneId.of(timezone) : java.time.ZoneId.systemDefault();
            foodLog.setLogDate(java.time.LocalDate.now(zone));
            foodLog.setTimeLogged(java.time.LocalDateTime.now(zone));
        } catch (Exception ignore) {}
        foodLogService.addFoodLog(foodLog);
        redirectAttributes.addFlashAttribute("success", "Food logged successfully!");
        return "redirect:/dashboard";
    }

    @PostMapping("/quick-add")
    public String quickAddFromMealPlan(@AuthenticationPrincipal OAuth2User principal,
                                       @RequestParam String mealName,
                                       @RequestParam String foodDescription,
                                       @RequestParam int calories,
                                       @RequestParam int protein,
                                       @RequestParam int carbs,
                                       @RequestParam int fat,
                                       @RequestParam(required = false, name = "timezone") String timezone,
                                       RedirectAttributes redirectAttributes) {
        User user = resolveUser(principal);
        FoodLog foodLog = new FoodLog();
        foodLog.setUser(user);
        foodLog.setMealName(mealName);
        foodLog.setFoodDescription(foodDescription);
        foodLog.setCalories(calories);
        foodLog.setProtein(protein);
        foodLog.setCarbs(carbs);
        foodLog.setFat(fat);
        foodLog.setFromMealPlan(true);
        try {
            java.time.ZoneId zone = (timezone != null && !timezone.isBlank()) ? java.time.ZoneId.of(timezone) : java.time.ZoneId.systemDefault();
            foodLog.setLogDate(java.time.LocalDate.now(zone));
            foodLog.setTimeLogged(java.time.LocalDateTime.now(zone));
        } catch (Exception ignore) {}
        foodLogService.addFoodLog(foodLog);
        redirectAttributes.addFlashAttribute("success", "Meal logged successfully!");
        return "redirect:/dashboard";
    }

    @PostMapping("/delete/{id}")
    public String deleteFoodLog(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        foodLogService.deleteFoodLog(id);
        redirectAttributes.addFlashAttribute("success", "Food log deleted!");
        return "redirect:/dashboard";
    }

    @PostMapping("/random-suggestion")
    @ResponseBody
    public org.springframework.http.ResponseEntity<java.util.Map<String, Object>> getRandomSuggestion(
            @RequestParam(required = false) Integer targetCalories) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        try {
            if (targetCalories == null) targetCalories = 500;
            java.util.Map<String, Object> suggestion = foodLogService.generateRandomHealthyMeal(targetCalories);
            if (suggestion == null) {
                response.put("success", false);
                response.put("error", "Could not generate suggestion.");
                return org.springframework.http.ResponseEntity.ok(response);
            }
            response.put("success", true);
            response.put("foodDescription", suggestion.get("description"));
            response.put("calories", suggestion.get("calories"));
            response.put("protein", suggestion.get("protein"));
            response.put("carbs", suggestion.get("carbs"));
            response.put("fat", suggestion.get("fat"));
            return org.springframework.http.ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Error generating suggestion.");
            return org.springframework.http.ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/estimate-with-alternative")
    @ResponseBody
    public org.springframework.http.ResponseEntity<java.util.Map<String, Object>> estimateWithAlternative(
            @RequestParam String foodDescription) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        try {
            java.util.Map<String, Integer> macros = foodLogService.estimateMacrosWithAI(foodDescription);
            if (macros == null) {
                response.put("success", false);
                response.put("error", "Please enter a valid food description.");
                return org.springframework.http.ResponseEntity.ok(response);
            }
            response.put("success", true);
            response.put("calories", macros.get("calories"));
            response.put("protein", macros.get("protein"));
            response.put("carbs", macros.get("carbs"));
            response.put("fat", macros.get("fat"));
            String healthier = foodLogService.suggestHealthierAlternative(
                    foodDescription,
                    macros.get("calories"),
                    macros.get("protein"),
                    macros.get("carbs"),
                    macros.get("fat")
            );
            if (healthier != null && !healthier.isBlank()) {
                response.put("healthierAlternative", healthier);
            }
            return org.springframework.http.ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Could not estimate macros.");
            return org.springframework.http.ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/history")
    public String viewHistory(@AuthenticationPrincipal OAuth2User principal, Model model) {
        User user = resolveUser(principal);
        List<FoodLog> last7Days = foodLogService.getLast7DaysFoodLogs(user);
        Map<LocalDate, Map<String, Integer>> dailyTotalsMap = new HashMap<>();
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            LocalDate date = today.minusDays(i);
            dailyTotalsMap.put(date, foodLogService.calculateDailyTotals(user, date));
        }
        model.addAttribute("user", user);
        model.addAttribute("foodLogs", last7Days);
        model.addAttribute("dailyTotalsMap", dailyTotalsMap);
        return "food-log-history";
    }

    private User resolveUser(OAuth2User principal) {
        if (principal == null) throw new RuntimeException("Not authenticated");
        String email = principal.getAttribute("email");
        String googleId = principal.getAttribute("sub");
        User user = null;
        if (email != null) user = userRepository.findByEmail(email).orElse(null);
        if (user == null && googleId != null) user = userRepository.findByGoogleId(googleId).orElse(null);
        if (user == null) throw new RuntimeException("User not found");
        return user;
    }

    @PostMapping("/estimate-macros")
    @ResponseBody
    public org.springframework.http.ResponseEntity<java.util.Map<String, Object>> estimateMacros(@RequestParam String foodDescription) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        try {
            java.util.Map<String, Integer> macros = foodLogService.estimateMacrosWithAI(foodDescription);
            if (macros == null) {
                response.put("success", false);
                response.put("error", "Please enter a valid food description.");
                return org.springframework.http.ResponseEntity.ok(response);
            }
            response.put("success", true);
            response.put("calories", macros.get("calories"));
            response.put("protein", macros.get("protein"));
            response.put("carbs", macros.get("carbs"));
            response.put("fat", macros.get("fat"));
            return org.springframework.http.ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Couldn't estimate macros. Please enter them manually.");
            return org.springframework.http.ResponseEntity.status(500).body(response);
        }
    }
}
