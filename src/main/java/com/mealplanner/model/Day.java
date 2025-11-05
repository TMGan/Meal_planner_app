package com.mealplanner.model;

import java.util.ArrayList;
import java.util.List;

public class Day {
    private int dayNumber; // 1, 2, 3
    private List<Meal> meals;
    private MacroTargets dailyTotal;

    public Day() {
        this.meals = new ArrayList<>();
    }

    public Day(int dayNumber, List<Meal> meals, MacroTargets dailyTotal) {
        this.dayNumber = dayNumber;
        this.meals = meals != null ? meals : new ArrayList<>();
        this.dailyTotal = dailyTotal;
    }

    public int getDayNumber() {
        return dayNumber;
    }

    public void setDayNumber(int dayNumber) {
        this.dayNumber = dayNumber;
    }

    public List<Meal> getMeals() {
        return meals;
    }

    public void setMeals(List<Meal> meals) {
        this.meals = meals;
    }

    public MacroTargets getDailyTotal() {
        return dailyTotal;
    }

    public void setDailyTotal(MacroTargets dailyTotal) {
        this.dailyTotal = dailyTotal;
    }
}

