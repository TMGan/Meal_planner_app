package com.mealplanner.repository;

import com.mealplanner.model.SavedMealPlan;
import com.mealplanner.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavedMealPlanRepository extends JpaRepository<SavedMealPlan, Long> {
    List<SavedMealPlan> findByUserOrderByCreatedAtDesc(User user);
    List<SavedMealPlan> findTop10ByUserOrderByCreatedAtDesc(User user);
}

