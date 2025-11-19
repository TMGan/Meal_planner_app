package com.mealplanner.controller;

import com.mealplanner.model.*;
import com.mealplanner.service.MacroCalculatorService;
import com.mealplanner.repository.UserRepository;
import com.mealplanner.repository.SavedMealPlanRepository;
import com.mealplanner.model.SavedMealPlan;
import com.mealplanner.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import com.mealplanner.service.MealPlanService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@Controller
public class MainController {

    private final MacroCalculatorService macroService;
    private final MealPlanService mealPlanService;
    private final UserRepository userRepository;
    private final SavedMealPlanRepository savedMealPlanRepository;
    private final com.mealplanner.service.UserFoodPreferencesService preferencesService;
    private final com.mealplanner.service.SwapService swapService;

    public MainController(MacroCalculatorService macroService,
                          MealPlanService mealPlanService,
                          UserRepository userRepository,
                          SavedMealPlanRepository savedMealPlanRepository,
                          com.mealplanner.service.UserFoodPreferencesService preferencesService,
                          com.mealplanner.service.SwapService swapService) {
        this.macroService = macroService;
        this.mealPlanService = mealPlanService;
        this.userRepository = userRepository;
        this.savedMealPlanRepository = savedMealPlanRepository;
        this.preferencesService = preferencesService;
        this.swapService = swapService;
    }

    @GetMapping("/")
    public String root(org.springframework.security.core.Authentication authentication) {
        if (authentication != null && (authentication.getPrincipal() instanceof org.springframework.security.oauth2.core.user.OAuth2User)) {
            return "redirect:/dashboard";
        }
        return "redirect:/home";
    }

    @GetMapping("/form")
    public String form(Model model) {
        model.addAttribute("activityLevels", activityLevels());
        model.addAttribute("goals", goals());
        model.addAttribute("allergyOptions", allergyOptions());
        return "form"; // templates/form.html
    }

    @PostMapping("/generate")
    public String generate(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestParam double weight,
            @RequestParam int heightFeet,
            @RequestParam int heightInches,
            @RequestParam int age,
            @RequestParam String sex,
            @RequestParam String activityLevel,
            @RequestParam String fitnessGoal,
            @RequestParam(name = "bmrFormula", required = false, defaultValue = "mifflin-st-jeor") String bmrFormula,
            @RequestParam(name = "bodyFatPercentage", required = false) Double bodyFatPercentage,
            @RequestParam(name = "proteinPercent", required = false, defaultValue = "30") int proteinPercent,
            @RequestParam(name = "carbsPercent", required = false, defaultValue = "40") int carbsPercent,
            @RequestParam(name = "fatPercent", required = false, defaultValue = "30") int fatPercent,
            @RequestParam(required = false) List<String> allergies,
            @RequestParam(required = false, name = "otherAllergies") String otherAllergies,
            Model model
    ) {
        List<String> errors = validateInputs(weight, heightFeet, heightInches, age, sex, activityLevel, fitnessGoal);
        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            model.addAttribute("activityLevels", activityLevels());
            model.addAttribute("goals", goals());
            model.addAttribute("allergyOptions", allergyOptions());
            return "form";
        }

        List<String> allAllergies = new ArrayList<>();
        if (allergies != null) allAllergies.addAll(allergies);
        if (StringUtils.hasText(otherAllergies)) {
            Arrays.stream(otherAllergies.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .forEach(allAllergies::add);
        }

        UserProfile profile = new UserProfile(weight, heightFeet, heightInches, age,
                sex, activityLevel, fitnessGoal, allAllergies);

        double bmr;
        try {
            bmr = macroService.calculateBMRByFormula(bmrFormula, weight, heightFeet, heightInches, age, sex, bodyFatPercentage);
        } catch (IllegalArgumentException iae) {
            model.addAttribute("errors", java.util.List.of(iae.getMessage()));
            model.addAttribute("activityLevels", activityLevels());
            model.addAttribute("goals", goals());
            model.addAttribute("allergyOptions", allergyOptions());
            return "form";
        }
        double tdee = macroService.calculateTDEE(bmr, activityLevel);
        double targetCalories = macroService.adjustForGoal(tdee, fitnessGoal);
        MacroTargets targets;
        try {
            targets = macroService.calculateMacros(targetCalories, proteinPercent, carbsPercent, fatPercent);
        } catch (IllegalArgumentException iae) {
            model.addAttribute("errors", java.util.List.of(iae.getMessage()));
            model.addAttribute("activityLevels", activityLevels());
            model.addAttribute("goals", goals());
            model.addAttribute("allergyOptions", allergyOptions());
            return "form";
        }

        try {
            MealPlan mealPlan;
            String preferencesExtra = null;
            if (principal != null) {
                String email = principal.getAttribute("email");
                User userPref = email != null ? userRepository.findByEmail(email).orElse(null) : null;
                if (userPref == null) {
                    String gid = principal.getAttribute("sub");
                    if (gid != null) userPref = userRepository.findByGoogleId(gid).orElse(null);
                }
                if (userPref != null) {
                    preferencesExtra = preferencesService.buildPreferencesPrompt(userPref);
                }
            }
            String learnedExtra = null;
            if (principal != null) {
                String email2 = principal.getAttribute("email");
                User user2 = email2 != null ? userRepository.findByEmail(email2).orElse(null) : null;
                if (user2 == null) {
                    String gid2 = principal.getAttribute("sub");
                    if (gid2 != null) user2 = userRepository.findByGoogleId(gid2).orElse(null);
                }
                if (user2 != null) learnedExtra = swapService.buildLearnedPreferencesPrompt(user2);
            }
            String combinedExtra = (preferencesExtra == null ? "" : preferencesExtra) + (learnedExtra == null ? "" : learnedExtra);
            if (!combinedExtra.isBlank()) {
                mealPlan = mealPlanService.generateMealPlan(profile, targets, combinedExtra);
            } else {
                mealPlan = mealPlanService.generateMealPlan(profile, targets);
            }
            GroceryList groceryList = mealPlanService.generateGroceryList(mealPlan);

            // Persist saved plan for the logged-in user
            if (principal != null) {
                String email = principal.getAttribute("email");
                if (email != null) {
                    User user = userRepository.findByEmail(email).orElse(null);
                    if (user != null) {
                        SavedMealPlan saved = new SavedMealPlan();
                        saved.setUser(user);
                        saved.setWeight(profile.getWeight());
                        saved.setHeightFeet(profile.getHeightFeet());
                        saved.setHeightInches(profile.getHeightInches());
                        saved.setAge(profile.getAge());
                        saved.setSex(profile.getSex());
                        saved.setActivityLevel(profile.getActivityLevel());
                        saved.setFitnessGoal(profile.getFitnessGoal());
                        saved.setTargetCalories(targets.getCalories());
                        saved.setTargetProtein(targets.getProtein());
                        saved.setTargetCarbs(targets.getCarbs());
                        saved.setTargetFat(targets.getFat());
                        try {
                            // Optional: reflect selected formula in saved JSON metadata later if desired
                            // Not persisting formula fields to entity to keep migration simple
                        } catch (Exception ignored) {}
                        try {
                            ObjectMapper mapper = new ObjectMapper();
                            saved.setMealPlanJson(mapper.writeValueAsString(mealPlan));
                            saved.setGroceryListJson(mapper.writeValueAsString(groceryList));
                        } catch (Exception ignored) {}
                        savedMealPlanRepository.save(saved);
                    }
                }
            }

            model.addAttribute("targets", targets);
            model.addAttribute("mealPlan", mealPlan);
            model.addAttribute("groceryList", groceryList);
            return "results"; // templates/results.html
        } catch (Exception ex) {
            model.addAttribute("errors", List.of("Failed to generate meal plan: " + ex.getMessage()));
            model.addAttribute("activityLevels", activityLevels());
            model.addAttribute("goals", goals());
            model.addAttribute("allergyOptions", allergyOptions());
            return "form";
        }
    }

    private List<String> validateInputs(double weight, int feet, int inches, int age, String sex,
                                        String activity, String goal) {
        List<String> errs = new ArrayList<>();
        if (weight < 50 || weight > 500) errs.add("Weight must be between 50 and 500 lbs.");
        if (feet < 3 || feet > 8) errs.add("Height feet must be between 3 and 8.");
        if (inches < 0 || inches > 11) errs.add("Height inches must be between 0 and 11.");
        if (age < 13 || age > 100) errs.add("Age must be between 13 and 100.");
        if (!List.of("male", "female").contains(Optional.ofNullable(sex).orElse("").toLowerCase())) {
            errs.add("Sex must be Male or Female.");
        }
        if (!activityLevels().contains(activity)) errs.add("Invalid activity level.");
        if (!goals().contains(goal)) errs.add("Invalid goal.");
        return errs;
    }

    private List<String> activityLevels() {
        return List.of("Sedentary", "Occasionally Active", "Moderately Active", "Very Active");
    }

    private List<String> goals() {
        return List.of("Lose Weight", "Maintain Weight", "Build Muscle");
    }

    private List<String> allergyOptions() {
        return List.of("Dairy", "Eggs", "Peanuts", "Tree Nuts", "Soy", "Shellfish", "Fish", "Gluten/Wheat");
    }
}
