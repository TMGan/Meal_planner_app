package com.mealplanner.service;

import com.mealplanner.model.MacroTargets;
import org.springframework.stereotype.Service;

@Service
public class MacroCalculatorService {

    public double poundsToKg(double lbs) {
        return lbs * 0.45359237d;
    }

    public double heightToCm(int feet, int inches) {
        return (feet * 30.48d) + (inches * 2.54d);
    }

    // Default Mifflin-St Jeor BMR for backward compatibility
    public double calculateBMR(double weightLbs, int heightFeet, int heightInches, int age, String sex) {
        return calculateBMRByFormula("mifflin-st-jeor", weightLbs, heightFeet, heightInches, age, sex, null);
    }

    // Multi-formula BMR support
    public double calculateBMRByFormula(String formula,
                                        double weightLbs,
                                        int heightFeet,
                                        int heightInches,
                                        int age,
                                        String sex,
                                        Double bodyFatPercentage) {
        String f = (formula == null || formula.isBlank()) ? "mifflin-st-jeor" : formula.toLowerCase();
        double weightKg = poundsToKg(weightLbs);
        double heightCm = heightToCm(heightFeet, heightInches);

        switch (f) {
            case "harris-benedict":
                if (sex != null && sex.equalsIgnoreCase("male")) {
                    return 88.362 + (13.397 * weightKg) + (4.799 * heightCm) - (5.677 * age);
                } else {
                    return 447.593 + (9.247 * weightKg) + (3.098 * heightCm) - (4.330 * age);
                }
            case "katch-mcardle":
                if (bodyFatPercentage == null) {
                    throw new IllegalArgumentException("Body fat percentage is required for Katch-McArdle formula");
                }
                if (bodyFatPercentage < 5 || bodyFatPercentage > 50) {
                    throw new IllegalArgumentException("Body fat percentage must be between 5% and 50%");
                }
                double leanBodyMassKg = weightKg * (1 - (bodyFatPercentage / 100.0));
                return 370 + (21.6 * leanBodyMassKg);
            case "mifflin-st-jeor":
            default:
                double base = (10 * weightKg) + (6.25 * heightCm) - (5 * age);
                if (sex != null && sex.equalsIgnoreCase("female")) {
                    return base - 161;
                }
                return base + 5;
        }
    }

    // activityLevel options mapped to multipliers; support legacy labels as synonyms
    public double calculateTDEE(double bmr, String activityLevel) {
        String lvl = activityLevel == null ? "" : activityLevel.toLowerCase().trim();
        double multiplier;
        switch (lvl) {
            case "sedentary" -> multiplier = 1.2;
            case "occasionally active" -> multiplier = 1.375;
            case "lightly active" -> multiplier = 1.375; // legacy/simple synonym
            case "lightly active (exercise 1-2x a week)" -> multiplier = 1.375; // legacy label
            case "moderately active" -> multiplier = 1.55;
            case "moderately active(exercise 3x a week)" -> multiplier = 1.55; // legacy label
            case "very active" -> multiplier = 1.725;
            case "very active(exercise 4-5x a week)" -> multiplier = 1.725; // legacy label
            case "extremely active" -> multiplier = 1.9;
            case "extremely active (exercise 5-7x a week)" -> multiplier = 1.9; // legacy label
            default -> multiplier = 1.2;
        }
        return bmr * multiplier;
    }

    // goal: Lose Weight, Maintain Weight, Build Muscle
    public double adjustForGoal(double tdee, String goal) {
        if (goal == null) return tdee;
        String g = goal.toLowerCase();
        if (g.contains("lose")) {
            return tdee - 350; // deficit midpoint
        } else if (g.contains("build") || g.contains("gain")) {
            return tdee + 350; // surplus midpoint
        }
        return tdee; // maintain
    }

    public MacroTargets calculateMacros(double targetCalories) {
        // 30/40/30 split
        double proteinCalories = targetCalories * 0.30;
        double carbCalories = targetCalories * 0.40;
        double fatCalories = targetCalories * 0.30;

        int protein = (int) Math.round(proteinCalories / 4.0);
        int carbs = (int) Math.round(carbCalories / 4.0);
        int fat = (int) Math.round(fatCalories / 9.0);
        int calories = (int) Math.round(targetCalories);
        return new MacroTargets(calories, protein, carbs, fat);
    }

    // Customizable macro split using percentages (P/C/F must sum to 100)
    public MacroTargets calculateMacros(double totalCalories, int proteinPercent, int carbsPercent, int fatPercent) {
        int total = proteinPercent + carbsPercent + fatPercent;
        if (total != 100) {
            throw new IllegalArgumentException("Macro percentages must total 100%. Current total: " + total + "%");
        }
        if (proteinPercent < 20 || proteinPercent > 50) {
            throw new IllegalArgumentException("Protein must be between 20% and 50%. Current: " + proteinPercent + "%");
        }
        if (carbsPercent < 20 || carbsPercent > 60) {
            throw new IllegalArgumentException("Carbs must be between 20% and 60%. Current: " + carbsPercent + "%");
        }
        if (fatPercent < 15 || fatPercent > 45) {
            throw new IllegalArgumentException("Fat must be between 15% and 45%. Current: " + fatPercent + "%");
        }

        double proteinCalories = totalCalories * proteinPercent / 100.0;
        double carbCalories = totalCalories * carbsPercent / 100.0;
        double fatCalories = totalCalories * fatPercent / 100.0;

        int protein = (int) Math.round(proteinCalories / 4.0);
        int carbs = (int) Math.round(carbCalories / 4.0);
        int fat = (int) Math.round(fatCalories / 9.0);
        int calories = (int) Math.round(totalCalories);
        return new MacroTargets(calories, protein, carbs, fat);
    }
}
