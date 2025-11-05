package com.mealplanner.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "saved_meal_plans")
public class SavedMealPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDateTime createdAt;

    // snapshot of user profile at generation time
    private double weight;
    private int heightFeet;
    private int heightInches;
    private int age;
    private String sex;
    private String activityLevel;
    private String fitnessGoal;

    // macro targets
    private int targetCalories;
    private int targetProtein;
    private int targetCarbs;
    private int targetFat;

    @Column(columnDefinition = "TEXT")
    private String mealPlanJson;

    @Column(columnDefinition = "TEXT")
    private String groceryListJson;

    public SavedMealPlan() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }
    public int getHeightFeet() { return heightFeet; }
    public void setHeightFeet(int heightFeet) { this.heightFeet = heightFeet; }
    public int getHeightInches() { return heightInches; }
    public void setHeightInches(int heightInches) { this.heightInches = heightInches; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }
    public String getActivityLevel() { return activityLevel; }
    public void setActivityLevel(String activityLevel) { this.activityLevel = activityLevel; }
    public String getFitnessGoal() { return fitnessGoal; }
    public void setFitnessGoal(String fitnessGoal) { this.fitnessGoal = fitnessGoal; }
    public int getTargetCalories() { return targetCalories; }
    public void setTargetCalories(int targetCalories) { this.targetCalories = targetCalories; }
    public int getTargetProtein() { return targetProtein; }
    public void setTargetProtein(int targetProtein) { this.targetProtein = targetProtein; }
    public int getTargetCarbs() { return targetCarbs; }
    public void setTargetCarbs(int targetCarbs) { this.targetCarbs = targetCarbs; }
    public int getTargetFat() { return targetFat; }
    public void setTargetFat(int targetFat) { this.targetFat = targetFat; }
    public String getMealPlanJson() { return mealPlanJson; }
    public void setMealPlanJson(String mealPlanJson) { this.mealPlanJson = mealPlanJson; }
    public String getGroceryListJson() { return groceryListJson; }
    public void setGroceryListJson(String groceryListJson) { this.groceryListJson = groceryListJson; }
}

