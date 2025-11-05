package com.mealplanner.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "food_logs")
public class FoodLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(name = "time_logged")
    private LocalDateTime timeLogged;

    @Column(name = "meal_name", length = 100)
    private String mealName;

    @Column(name = "food_description", columnDefinition = "TEXT", nullable = false)
    private String foodDescription;

    @Column(nullable = false)
    private int calories;

    @Column(nullable = false)
    private int protein;

    @Column(nullable = false)
    private int carbs;

    @Column(nullable = false)
    private int fat;

    @Column(name = "from_meal_plan")
    private boolean fromMealPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_plan_id")
    private SavedMealPlan mealPlan;

    public FoodLog() {
        this.timeLogged = LocalDateTime.now();
        this.logDate = LocalDate.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public LocalDate getLogDate() { return logDate; }
    public void setLogDate(LocalDate logDate) { this.logDate = logDate; }
    public LocalDateTime getTimeLogged() { return timeLogged; }
    public void setTimeLogged(LocalDateTime timeLogged) { this.timeLogged = timeLogged; }
    public String getMealName() { return mealName; }
    public void setMealName(String mealName) { this.mealName = mealName; }
    public String getFoodDescription() { return foodDescription; }
    public void setFoodDescription(String foodDescription) { this.foodDescription = foodDescription; }
    public int getCalories() { return calories; }
    public void setCalories(int calories) { this.calories = calories; }
    public int getProtein() { return protein; }
    public void setProtein(int protein) { this.protein = protein; }
    public int getCarbs() { return carbs; }
    public void setCarbs(int carbs) { this.carbs = carbs; }
    public int getFat() { return fat; }
    public void setFat(int fat) { this.fat = fat; }
    public boolean isFromMealPlan() { return fromMealPlan; }
    public void setFromMealPlan(boolean fromMealPlan) { this.fromMealPlan = fromMealPlan; }
    public SavedMealPlan getMealPlan() { return mealPlan; }
    public void setMealPlan(SavedMealPlan mealPlan) { this.mealPlan = mealPlan; }
}

