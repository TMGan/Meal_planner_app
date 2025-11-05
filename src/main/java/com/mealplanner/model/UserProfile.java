package com.mealplanner.model;

import java.util.ArrayList;
import java.util.List;

public class UserProfile {
    private double weight; // lbs
    private int heightFeet;
    private int heightInches;
    private int age;
    private String sex; // "Male" or "Female"
    private String activityLevel; // e.g., Sedentary, Lightly Active, etc.
    private String fitnessGoal; // Lose Weight, Maintain Weight, Build Muscle
    private List<String> allergies;

    public UserProfile() {
        this.allergies = new ArrayList<>();
    }

    public UserProfile(double weight, int heightFeet, int heightInches, int age,
                       String sex, String activityLevel, String fitnessGoal, List<String> allergies) {
        this.weight = weight;
        this.heightFeet = heightFeet;
        this.heightInches = heightInches;
        this.age = age;
        this.sex = sex;
        this.activityLevel = activityLevel;
        this.fitnessGoal = fitnessGoal;
        this.allergies = allergies != null ? allergies : new ArrayList<>();
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public int getHeightFeet() {
        return heightFeet;
    }

    public void setHeightFeet(int heightFeet) {
        this.heightFeet = heightFeet;
    }

    public int getHeightInches() {
        return heightInches;
    }

    public void setHeightInches(int heightInches) {
        this.heightInches = heightInches;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getActivityLevel() {
        return activityLevel;
    }

    public void setActivityLevel(String activityLevel) {
        this.activityLevel = activityLevel;
    }

    public String getFitnessGoal() {
        return fitnessGoal;
    }

    public void setFitnessGoal(String fitnessGoal) {
        this.fitnessGoal = fitnessGoal;
    }

    public List<String> getAllergies() {
        return allergies;
    }

    public void setAllergies(List<String> allergies) {
        this.allergies = allergies;
    }
}

