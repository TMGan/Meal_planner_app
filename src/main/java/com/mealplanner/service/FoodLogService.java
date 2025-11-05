package com.mealplanner.service;

import com.mealplanner.model.FoodLog;
import com.mealplanner.model.User;
import com.mealplanner.repository.FoodLogRepository;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FoodLogService {

    private final FoodLogRepository foodLogRepository;
    private final Environment env;

    public FoodLogService(FoodLogRepository foodLogRepository, Environment env) {
        this.foodLogRepository = foodLogRepository;
        this.env = env;
    }

    public FoodLog addFoodLog(FoodLog foodLog) {
        return foodLogRepository.save(foodLog);
    }

    public List<FoodLog> getTodaysFoodLogs(User user) {
        LocalDate today = LocalDate.now();
        return foodLogRepository.findByUserAndLogDateOrderByTimeLoggedAsc(user, today);
    }

    public List<FoodLog> getFoodLogsByDate(User user, LocalDate date) {
        return foodLogRepository.findByUserAndLogDateOrderByTimeLoggedAsc(user, date);
    }

    public List<FoodLog> getLast7DaysFoodLogs(User user) {
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(6);
        return foodLogRepository.findByUserAndLogDateBetweenOrderByLogDateDescTimeLoggedAsc(user, sevenDaysAgo, today);
    }

    public Map<String, Integer> calculateTodaysTotals(User user) {
        LocalDate today = LocalDate.now();
        return calculateDailyTotals(user, today);
    }

    public Map<String, Integer> calculateDailyTotals(User user, LocalDate date) {
        java.util.List<Object[]> rows = foodLogRepository.calculateDailyTotals(user, date);
        Map<String, Integer> totals = new HashMap<>();
        if (rows != null && !rows.isEmpty()) {
            Object[] result = rows.get(0);
            totals.put("calories", ((Number) result[0]).intValue());
            totals.put("protein", ((Number) result[1]).intValue());
            totals.put("carbs", ((Number) result[2]).intValue());
            totals.put("fat", ((Number) result[3]).intValue());
        } else {
            totals.put("calories", 0);
            totals.put("protein", 0);
            totals.put("carbs", 0);
            totals.put("fat", 0);
        }
        return totals;
    }

    public void deleteFoodLog(Long id) {
        foodLogRepository.deleteById(id);
    }

    public FoodLog updateFoodLog(FoodLog foodLog) {
        return foodLogRepository.save(foodLog);
    }

    public FoodLog getFoodLogById(Long id) {
        return foodLogRepository.findById(id).orElse(null);
    }

    /**
     * Estimate macros from food description using AI.
     * Returns null if invalid or on errors per validation rules.
     */
    public Map<String, Integer> estimateMacrosWithAI(String foodDescription) {
        if (foodDescription == null) return null;
        String desc = foodDescription.trim();
        if (desc.isEmpty()) return null;
        if (desc.length() < 3) return null;
        if (desc.length() > 500) return null;

        try {
            // Mock mode for local/dev testing without network/API key
            String mock = env.getProperty("ai.mock", "false");
            if ("true".equalsIgnoreCase(mock)) {
                return estimateMacrosMock(desc);
            }
            String aiResponse = callClaudeAPI(desc);
            return parseMacroResponse(aiResponse);
        } catch (Exception e) {
            System.err.println("Error estimating macros: " + e.getMessage());
            return null;
        }
    }

    private String callClaudeAPI(String foodDescription) throws Exception {
        String apiKey = env.getProperty("ai.api.key");
        // Support both ai.anthropic.url and ai.api.url keys
        String apiUrl = env.getProperty("ai.anthropic.url", env.getProperty("ai.api.url"));
        // Support both ai.anthropic.model and ai.api.model keys
        String model = env.getProperty("ai.anthropic.model", env.getProperty("ai.api.model", "claude-3-5-sonnet-20241022"));

        if (apiKey == null || apiUrl == null) {
            throw new IllegalStateException("AI API configuration missing");
        }

        String prompt = buildMacroEstimationPromptV2(foodDescription);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("max_tokens", 200);
        requestBody.put("messages", java.util.List.of(
                java.util.Map.of("role", "user", "content", prompt)
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);
        headers.set("anthropic-version", "2023-06-01");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        int timeout = 10_000;
        try {
            timeout = Integer.parseInt(env.getProperty("ai.timeout.ms", "10000"));
        } catch (Exception ignore) {}
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        RestTemplate restTemplate = new RestTemplate(factory);
        ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);
        return response.getBody();
    }

    private String buildMacroEstimationPrompt(String foodDescription) {
        return String.format(
            "You are a nutrition expert who helps people track their macros in a realistic, non-judgmental way.\n\n" +
            "Your job: Analyze a food description and estimate macros.\n\n" +
            "IMPORTANT PHILOSOPHY:\n" +
            "- Accept ALL legitimate foods: fast food, frozen meals, packaged items, restaurant meals, convenience foods\n" +
            "- DO NOT judge food quality or suggest \"healthier\" alternatives\n" +
            "- Respect user preferences (white rice > brown rice is fine!)\n" +
            "- Goal is tracking macros, not lecturing about nutrition\n" +
            "- When in doubt, accept it and estimate\n\n" +
            "ONLY reject if the input is:\n" +
            "1. Actually not food (car, phone, furniture, etc.)\n" +
            "2. Inappropriate/offensive content\n" +
            "3. Complete nonsensical gibberish\n" +
            "4. Empty or meaningless\n\n" +
            "If REJECTED, respond with: INVALID\n\n" +
            "If ACCEPTED, estimate the macros and respond with ONLY these numbers in this exact format:\n" +
            "calories,protein,carbs,fat\n\n" +
            "EXAMPLES OF VALID INPUTS (accept these!):\n\n" +
            "Fast Food:\n" +
            "\"Chipotle chicken bowl white rice black beans\" → 650,45,70,18\n" +
            "\"Big Mac and medium fries\" → 1080,28,120,54\n" +
            "\"Subway 6-inch turkey sub\" → 280,18,46,4\n\n" +
            "Frozen/Packaged:\n" +
            "\"Hot Pocket pepperoni\" → 320,12,38,12\n" +
            "\"Lean Cuisine chicken pasta\" → 280,18,40,5\n" +
            "\"Quest protein bar chocolate chip\" → 200,20,22,9\n\n" +
            "Casual/Vague:\n" +
            "\"leftover pizza couple slices\" → 600,24,70,22\n" +
            "\"some chicken and rice\" → 450,40,50,8\n" +
            "\"idk breakfast stuff eggs and toast\" → 350,20,30,15\n\n" +
            "Convenience:\n" +
            "\"protein shake with banana\" → 280,30,35,4\n" +
            "\"Greek yogurt and granola\" → 320,15,50,8\n" +
            "\"deli sandwich\" → 400,25,45,12\n\n" +
            "EXAMPLES OF INVALID INPUTS (reject these):\n\n" +
            "Non-food:\n" +
            "\"car\" → INVALID\n" +
            "\"my phone\" → INVALID\n\n" +
            "Inappropriate:\n" +
            "\"penis\" → INVALID\n" +
            "\"fuck\" → INVALID\n\n" +
            "Gibberish:\n" +
            "\"asdfghjkl\" → INVALID\n" +
            "\"xxx\" → INVALID\n\n" +
            "Now analyze this food description:\n" +
            "\"%s\"\n\n" +
            "Your response (either \"INVALID\" or \"calories,protein,carbs,fat\"):",
            foodDescription
        );
    }

    // Updated prompt focused on USDA accuracy and precise numeric-only output
    private String buildMacroEstimationPromptV2(String foodDescription) {
        return String.format(
                "You are a professional fitness nutritionist with access to USDA FoodData Central.\n" +
                "Task: Analyze a food description and estimate macros ACCURATELY.\n\n" +
                "RULES:\n" +
                "- Use USDA standards for whole foods; account for cooked vs raw weights.\n" +
                "- Be precise: round to nearest 1g.\n" +
                "- Use realistic portions; if portion unspecified, assume typical serving (e.g., chicken cooked 6oz, rice 1 cup cooked).\n" +
                "- If uncertain, choose conservative estimates (avoid inflated numbers).\n" +
                "- Reject only non-food, offensive, or gibberish inputs.\n\n" +
                "OUTPUT: If invalid, respond EXACTLY: INVALID.\n" +
                "If valid, respond with ONLY: calories,protein,carbs,fat (numbers only, comma-separated).\n\n" +
                "Food: \"%s\"\n\n" +
                "Response:",
                foodDescription
        );
    }

    private Map<String, Integer> parseMacroResponse(String aiResponse) {
        if (aiResponse == null || aiResponse.isEmpty()) return null;
        try {
            String content = extractContentFromAPIResponse(aiResponse);
            if (content == null || content.trim().isEmpty()) return null;
            if (content.trim().equalsIgnoreCase("INVALID")) return null;

            String[] parts = content.trim().split(",");
            if (parts.length != 4) return null;

            Map<String, Integer> macros = new HashMap<>();
            macros.put("calories", Integer.parseInt(parts[0].trim()));
            macros.put("protein", Integer.parseInt(parts[1].trim()));
            macros.put("carbs", Integer.parseInt(parts[2].trim()));
            macros.put("fat", Integer.parseInt(parts[3].trim()));

            if (macros.get("calories") < 0 || macros.get("calories") > 5000) return null;
            if (macros.get("protein") < 0 || macros.get("protein") > 500) return null;
            if (macros.get("carbs") < 0 || macros.get("carbs") > 1000) return null;
            if (macros.get("fat") < 0 || macros.get("fat") > 500) return null;

            return macros;
        } catch (NumberFormatException e) {
            return null;
        } catch (Exception e) {
            System.err.println("Error parsing macro response: " + e.getMessage());
            return null;
        }
    }

    private String extractContentFromAPIResponse(String jsonResponse) {
        try {
            int textIndex = jsonResponse.indexOf("\"text\":");
            if (textIndex == -1) return null;
            int startQuote = jsonResponse.indexOf('"', textIndex + 7);
            if (startQuote == -1) return null;
            int endQuote = jsonResponse.indexOf('"', startQuote + 1);
            if (endQuote == -1) return null;
            return jsonResponse.substring(startQuote + 1, endQuote);
        } catch (Exception e) {
            return null;
        }
    }

    // Simple mock estimator for dev/testing without external API
    private Map<String, Integer> estimateMacrosMock(String desc) {
        String lower = desc.toLowerCase();
        // Basic invalid content checks
        String[] banned = {"penis","fuck","shit","car","phone","laptop","table","chair","asdf","xxx"};
        for (String b : banned) {
            if (lower.contains(b)) return null;
        }
        if (!lower.matches(".*[a-z].*")) return null; // must contain letters

        int words = Math.max(1, desc.split("\\s+").length);
        int calories = Math.min(800, 120 + words * 80);
        int protein = Math.min(80, words * 8);
        int carbs = Math.min(120, words * 15);
        int fat = Math.min(40, words * 5);

        Map<String, Integer> out = new HashMap<>();
        out.put("calories", calories);
        out.put("protein", protein);
        out.put("carbs", carbs);
        out.put("fat", fat);
        return out;
    }

    // --- Random healthy meal suggestion ---
    public Map<String, Object> generateRandomHealthyMeal(int targetCalories) {
        try {
            String mock = env.getProperty("ai.mock", "false");
            if ("true".equalsIgnoreCase(mock)) {
                return randomMealMock(targetCalories);
            }
            String prompt = buildRandomMealPrompt(targetCalories);
            String aiResponse = callClaudeAPI(prompt);
            return parseRandomMealResponse(aiResponse);
        } catch (Exception e) {
            System.err.println("Error generating random meal: " + e.getMessage());
            return null;
        }
    }

    private String buildRandomMealPrompt(int targetCalories) {
        return String.format(
                "Generate a healthy, balanced meal suggestion with approximately %d calories.\n\n" +
                        "REQUIREMENTS:\n" +
                        "- Use whole foods (chicken, salmon, eggs, white rice, sweet potato, vegetables, etc.)\n" +
                        "- Balanced macros (30%% protein, 40%% carbs, 30%% fat)\n" +
                        "- Simple, realistic meal that someone can make or buy\n" +
                        "- Be creative but practical\n\n" +
                        "RESPOND in this exact format:\n" +
                        "description: [Food description]\n" +
                        "calories: [number]\n" +
                        "protein: [number]\n" +
                        "carbs: [number]\n" +
                        "fat: [number]\n\n" +
                        "EXAMPLE:\n" +
                        "description: Grilled chicken breast (6oz), white rice (1 cup), steamed broccoli\n" +
                        "calories: 520\n" +
                        "protein: 52\n" +
                        "carbs: 60\n" +
                        "fat: 8\n\n" +
                        "Now generate a different random healthy meal around %d calories:",
                targetCalories, targetCalories
        );
    }

    private Map<String, Object> parseRandomMealResponse(String aiResponse) {
        try {
            String content = extractContentFromAPIResponse(aiResponse);
            if (content == null) return null;
            Map<String, Object> meal = new HashMap<>();
            for (String raw : content.split("\n")) {
                String line = raw.trim();
                if (line.toLowerCase().startsWith("description:")) {
                    meal.put("description", line.substring("description:".length()).trim());
                } else if (line.toLowerCase().startsWith("calories:")) {
                    meal.put("calories", Integer.parseInt(line.substring("calories:".length()).trim()));
                } else if (line.toLowerCase().startsWith("protein:")) {
                    meal.put("protein", Integer.parseInt(line.substring("protein:".length()).trim()));
                } else if (line.toLowerCase().startsWith("carbs:")) {
                    meal.put("carbs", Integer.parseInt(line.substring("carbs:".length()).trim()));
                } else if (line.toLowerCase().startsWith("fat:")) {
                    meal.put("fat", Integer.parseInt(line.substring("fat:".length()).trim()));
                }
            }
            return meal.containsKey("description") ? meal : null;
        } catch (Exception e) {
            return null;
        }
    }

    public String suggestHealthierAlternative(String originalFood, int calories, int protein, int carbs, int fat) {
        String lower = originalFood.toLowerCase();
        boolean needsAlt = lower.contains("hot pocket") || lower.contains("pizza") || lower.contains("mcdonald")
                || lower.contains("burger king") || lower.contains("taco bell") || lower.contains("fast food")
                || lower.contains("frozen");
        if (!needsAlt) return null;
        try {
            String prompt = buildHealthierAlternativePrompt(originalFood, calories, protein, carbs, fat);
            String resp = callClaudeAPI(prompt);
            return extractContentFromAPIResponse(resp);
        } catch (Exception e) {
            return null;
        }
    }

    private String buildHealthierAlternativePrompt(String originalFood, int cal, int p, int c, int f) {
        return String.format(
                "The user ate: \"%s\" (%d cal, %dg P, %dg C, %dg F)\n\n" +
                        "Suggest a healthier whole food alternative with similar macros.\n\n" +
                        "REQUIREMENTS:\n" +
                        "- Similar total calories (within 50 cal)\n" +
                        "- Similar macros (within 10g each)\n" +
                        "- Use whole foods (chicken, fish, eggs, white rice, vegetables, etc.)\n" +
                        "- Be realistic and practical\n" +
                        "- One short sentence suggestion\n\n" +
                        "EXAMPLE:\n" +
                        "Original: Hot Pocket (320 cal, 12g P, 38g C, 12g F)\n" +
                        "Suggestion: Grilled chicken wrap with whole wheat tortilla and veggies\n\n" +
                        "Your suggestion (one sentence only):",
                originalFood, cal, p, c, f
        );
    }

    private Map<String, Object> randomMealMock(int targetCalories) {
        String[] options = new String[] {
                "Grilled chicken (6oz), white rice (1 cup), steamed broccoli",
                "Salmon (5oz), sweet potato (1 medium), asparagus",
                "Turkey chili (1.5 cups), avocado (1/4)",
                "Greek yogurt (1 cup), granola (1/2 cup), blueberries",
                "Egg omelette (3 eggs) with spinach, whole wheat toast (1 slice)"
        };
        int idx = Math.abs((int)System.nanoTime()) % options.length;
        int cal = Math.max(400, Math.min(700, targetCalories));
        Map<String, Object> meal = new HashMap<>();
        meal.put("description", options[idx]);
        meal.put("calories", cal);
        meal.put("protein", (int)Math.round(cal * 0.3 / 4));
        meal.put("carbs", (int)Math.round(cal * 0.4 / 4));
        meal.put("fat", (int)Math.round(cal * 0.3 / 9));
        return meal;
    }
}
