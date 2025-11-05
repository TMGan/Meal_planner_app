package com.mealplanner.controller;

import com.mealplanner.model.MacroTargets;
import com.mealplanner.model.Meal;
import com.mealplanner.service.MealPlanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/meal")
public class MealApiController {

    private final MealPlanService mealPlanService;

    public MealApiController(MealPlanService mealPlanService) {
        this.mealPlanService = mealPlanService;
    }

    public static class SwapMealRequest {
        public String avoidSimilarTo; // optional hint (e.g., current recipe name)
        public int targetCalories;
        public int targetProtein;
        public int targetCarbs;
        public int targetFat;
    }

    @PostMapping("/swap")
    public ResponseEntity<?> swapMeal(@RequestBody SwapMealRequest req) {
        try {
            MacroTargets target = new MacroTargets(
                    req.targetCalories,
                    req.targetProtein,
                    req.targetCarbs,
                    req.targetFat
            );
            Meal replacement = mealPlanService.generateReplacementMeal(target, req.avoidSimilarTo);
            if (replacement == null) {
                return ResponseEntity.status(502).body(Map.of("error", "Could not generate replacement meal"));
            }
            return ResponseEntity.ok(replacement);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}

