package com.mealplanner.repository;

import com.mealplanner.model.FoodLog;
import com.mealplanner.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FoodLogRepository extends JpaRepository<FoodLog, Long> {

    List<FoodLog> findByUserAndLogDateOrderByTimeLoggedAsc(User user, LocalDate logDate);

    List<FoodLog> findByUserAndLogDateBetweenOrderByLogDateDescTimeLoggedAsc(User user, LocalDate startDate, LocalDate endDate);

    List<FoodLog> findByUserAndLogDate(User user, LocalDate logDate);

    @Query("SELECT COALESCE(SUM(f.calories),0), COALESCE(SUM(f.protein),0), COALESCE(SUM(f.carbs),0), COALESCE(SUM(f.fat),0) FROM FoodLog f WHERE f.user = :user AND f.logDate = :logDate")
    java.util.List<Object[]> calculateDailyTotals(@Param("user") User user, @Param("logDate") LocalDate logDate);

    // Admin helpers
    int countByUser(User user);

    @Query("SELECT COUNT(DISTINCT f.user) FROM FoodLog f WHERE f.logDate = :date")
    long countActiveUsersOnDate(@Param("date") LocalDate date);

    List<FoodLog> findAllByOrderByLogDateDesc();

    FoodLog findTopByUserOrderByLogDateDesc(User user);
}
