package com.mealplanner.service;

import com.mealplanner.model.User;
import com.mealplanner.model.UserFoodPreferences;
import com.mealplanner.repository.UserFoodPreferencesRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserFoodPreferencesService {

    private final UserFoodPreferencesRepository preferencesRepository;

    public UserFoodPreferencesService(UserFoodPreferencesRepository preferencesRepository) {
        this.preferencesRepository = preferencesRepository;
    }

    public UserFoodPreferences getUserPreferences(User user) {
        return preferencesRepository.findByUser(user).orElse(null);
    }

    public UserFoodPreferences savePreferences(User user,
                                               String preferredFoodsText,
                                               String avoidedFoodsText,
                                               String cookingPreference,
                                               String dietaryStyle) {
        UserFoodPreferences prefs = preferencesRepository.findByUser(user)
                .orElse(new UserFoodPreferences());
        prefs.setUser(user);
        prefs.setPreferredFoodsText(cleanFoodList(preferredFoodsText));
        prefs.setAvoidedFoodsText(cleanFoodList(avoidedFoodsText));
        prefs.setCookingPreference(cookingPreference);
        prefs.setDietaryStyle(dietaryStyle);
        return preferencesRepository.save(prefs);
    }

    private String cleanFoodList(String foodList) {
        if (foodList == null || foodList.trim().isEmpty()) return "";
        String[] items = foodList.split("[,\n]+");
        List<String> cleaned = Arrays.stream(items)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        return String.join(", ", cleaned);
    }

    public boolean hasPreferences(User user) {
        return preferencesRepository.existsByUser(user);
    }

    public String buildPreferencesPrompt(User user) {
        UserFoodPreferences prefs = getUserPreferences(user);
        if (prefs == null || (prefs.getPreferredFoodsText() == null || prefs.getPreferredFoodsText().isEmpty())) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("\n\nUSER'S FOOD PREFERENCES:\n");
        if (prefs.getPreferredFoodsText() != null && !prefs.getPreferredFoodsText().isEmpty()) {
            sb.append("- Foods they like and want: ").append(prefs.getPreferredFoodsText()).append("\n");
        }
        if (prefs.getAvoidedFoodsText() != null && !prefs.getAvoidedFoodsText().isEmpty()) {
            sb.append("- Foods to avoid: ").append(prefs.getAvoidedFoodsText()).append("\n");
        }
        if (prefs.getCookingPreference() != null && !prefs.getCookingPreference().isEmpty()) {
            sb.append("- Cooking preference: ").append(prefs.getCookingPreference()).append("\n");
        }
        if (prefs.getDietaryStyle() != null && !prefs.getDietaryStyle().isEmpty()) {
            sb.append("- Dietary style: ").append(prefs.getDietaryStyle()).append("\n");
        }
        sb.append("\nIMPORTANT: Use foods from their preferred list. Respect their preferences completely.\n");
        sb.append("Do NOT exclude convenient, packaged, or restaurant foods if they fit macros.\n");
        return sb.toString();
    }
}

