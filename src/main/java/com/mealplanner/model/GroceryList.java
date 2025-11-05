package com.mealplanner.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GroceryList {
    // Categories: Proteins, Dairy, Produce, Grains, Pantry, Other
    private Map<String, List<String>> categorizedItems;

    public GroceryList() {
        this.categorizedItems = new LinkedHashMap<>();
    }

    public GroceryList(Map<String, List<String>> categorizedItems) {
        this.categorizedItems = categorizedItems != null ? categorizedItems : new LinkedHashMap<>();
    }

    public Map<String, List<String>> getCategorizedItems() {
        return categorizedItems;
    }

    public void setCategorizedItems(Map<String, List<String>> categorizedItems) {
        this.categorizedItems = categorizedItems;
    }
}

