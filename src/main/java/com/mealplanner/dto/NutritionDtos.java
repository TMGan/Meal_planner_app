package com.mealplanner.dto;

import java.util.ArrayList;
import java.util.List;

public class NutritionDtos {
    public static class EstimateRequest {
        private String text;
        public EstimateRequest() {}
        public EstimateRequest(String text) { this.text = text; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }

    public static class Item {
        private String item;
        private double quantity;
        private String unit;
        private int calories;
        private int protein;
        private int carbs;
        private int fat;
        public String getItem() { return item; }
        public void setItem(String item) { this.item = item; }
        public double getQuantity() { return quantity; }
        public void setQuantity(double quantity) { this.quantity = quantity; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        public int getCalories() { return calories; }
        public void setCalories(int calories) { this.calories = calories; }
        public int getProtein() { return protein; }
        public void setProtein(int protein) { this.protein = protein; }
        public int getCarbs() { return carbs; }
        public void setCarbs(int carbs) { this.carbs = carbs; }
        public int getFat() { return fat; }
        public void setFat(int fat) { this.fat = fat; }
    }

    public static class EstimateResponse {
        private List<Item> items = new ArrayList<>();
        private int calories;
        private int protein;
        private int carbs;
        private int fat;
        private String source; // ai | table | hybrid
        public List<Item> getItems() { return items; }
        public void setItems(List<Item> items) { this.items = items; }
        public int getCalories() { return calories; }
        public void setCalories(int calories) { this.calories = calories; }
        public int getProtein() { return protein; }
        public void setProtein(int protein) { this.protein = protein; }
        public int getCarbs() { return carbs; }
        public void setCarbs(int carbs) { this.carbs = carbs; }
        public int getFat() { return fat; }
        public void setFat(int fat) { this.fat = fat; }
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
    }
}

