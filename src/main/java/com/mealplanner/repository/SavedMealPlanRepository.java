package com.mealplanner.repository;

import com.mealplanner.model.SavedMealPlan;
import com.mealplanner.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SavedMealPlanRepository extends JpaRepository<SavedMealPlan, Long> {
    List<SavedMealPlan> findByUserOrderByCreatedAtDesc(User user);
    List<SavedMealPlan> findTop10ByUserOrderByCreatedAtDesc(User user);
    List<SavedMealPlan> findAllByOrderByCreatedAtDesc();
    int countByUser(User user);
    long countByCreatedAtAfter(LocalDateTime date);
    long countByGenerationFailed(boolean failed);

    @Query("SELECT AVG(s.accuracyScore) FROM SavedMealPlan s WHERE s.accuracyScore IS NOT NULL")
    Double getAverageAccuracyScore();

    @Query("SELECT s FROM SavedMealPlan s WHERE s.accuracyScore < :threshold AND s.accuracyScore IS NOT NULL ORDER BY s.createdAt DESC")
    List<SavedMealPlan> findInaccuratePlans(@Param("threshold") double threshold);
}
