package com.mealplanner.model;

public class FoodItem {
    private String item;
    private String portion;

    public FoodItem() {
    }

    public FoodItem(String item, String portion) {
        this.item = item;
        this.portion = portion;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getPortion() {
        return portion;
    }

    public void setPortion(String portion) {
        this.portion = portion;
    }
}

