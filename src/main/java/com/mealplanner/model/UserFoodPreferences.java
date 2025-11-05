package com.mealplanner.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "user_food_preferences")
public class UserFoodPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "preferred_foods_text", columnDefinition = "TEXT")
    private String preferredFoodsText;

    @Column(name = "avoided_foods_text", columnDefinition = "TEXT")
    private String avoidedFoodsText;

    @Column(name = "cooking_preference", length = 50)
    private String cookingPreference;

    @Column(name = "dietary_style", length = 50)
    private String dietaryStyle;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public UserFoodPreferences() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public List<String> getPreferredFoods() {
        if (preferredFoodsText == null || preferredFoodsText.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(preferredFoodsText.split(","));
    }

    public void setPreferredFoods(List<String> foods) {
        this.preferredFoodsText = String.join(",", foods);
    }

    public List<String> getAvoidedFoods() {
        if (avoidedFoodsText == null || avoidedFoodsText.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(avoidedFoodsText.split(","));
    }

    public void setAvoidedFoods(List<String> foods) {
        this.avoidedFoodsText = String.join(",", foods);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getPreferredFoodsText() { return preferredFoodsText; }
    public void setPreferredFoodsText(String preferredFoodsText) { this.preferredFoodsText = preferredFoodsText; }
    public String getAvoidedFoodsText() { return avoidedFoodsText; }
    public void setAvoidedFoodsText(String avoidedFoodsText) { this.avoidedFoodsText = avoidedFoodsText; }
    public String getCookingPreference() { return cookingPreference; }
    public void setCookingPreference(String cookingPreference) { this.cookingPreference = cookingPreference; }
    public String getDietaryStyle() { return dietaryStyle; }
    public void setDietaryStyle(String dietaryStyle) { this.dietaryStyle = dietaryStyle; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PreUpdate
    public void preUpdate() { this.updatedAt = LocalDateTime.now(); }
}

