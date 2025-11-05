package com.mealplanner.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "learned_user_preferences")
public class LearnedUserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "preference_type", nullable = false, length = 50)
    private String preferenceType;

    @Column(name = "food_item", nullable = false)
    private String foodItem;

    @Column(name = "compared_to_food")
    private String comparedToFood;

    @Column(name = "confidence_score", precision = 3, scale = 2)
    private BigDecimal confidenceScore;

    @Column(name = "times_observed")
    private Integer timesObserved;

    @Column(name = "last_observed")
    private LocalDateTime lastObserved;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public LearnedUserPreference() {
        this.confidenceScore = new BigDecimal("0.50");
        this.timesObserved = 1;
        this.lastObserved = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getPreferenceType() { return preferenceType; }
    public void setPreferenceType(String preferenceType) { this.preferenceType = preferenceType; }
    public String getFoodItem() { return foodItem; }
    public void setFoodItem(String foodItem) { this.foodItem = foodItem; }
    public String getComparedToFood() { return comparedToFood; }
    public void setComparedToFood(String comparedToFood) { this.comparedToFood = comparedToFood; }
    public BigDecimal getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(BigDecimal confidenceScore) { this.confidenceScore = confidenceScore; }
    public Integer getTimesObserved() { return timesObserved; }
    public void setTimesObserved(Integer timesObserved) { this.timesObserved = timesObserved; }
    public LocalDateTime getLastObserved() { return lastObserved; }
    public void setLastObserved(LocalDateTime lastObserved) { this.lastObserved = lastObserved; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

