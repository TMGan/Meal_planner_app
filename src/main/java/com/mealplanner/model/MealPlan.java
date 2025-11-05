package com.mealplanner.model;

import java.util.ArrayList;
import java.util.List;

public class MealPlan {
    private List<Day> days;
    private MacroTargets dailyTargets;

    public MealPlan() {
        this.days = new ArrayList<>();
    }

    public MealPlan(List<Day> days, MacroTargets dailyTargets) {
        this.days = days != null ? days : new ArrayList<>();
        this.dailyTargets = dailyTargets;
    }

    public List<Day> getDays() {
        return days;
    }

    public void setDays(List<Day> days) {
        this.days = days;
    }

    public MacroTargets getDailyTargets() {
        return dailyTargets;
    }

    public void setDailyTargets(MacroTargets dailyTargets) {
        this.dailyTargets = dailyTargets;
    }
}

