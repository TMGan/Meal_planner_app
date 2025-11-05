package com.mealplanner.repository;

import com.mealplanner.model.User;
import com.mealplanner.model.UserFoodPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserFoodPreferencesRepository extends JpaRepository<UserFoodPreferences, Long> {
    Optional<UserFoodPreferences> findByUser(User user);
    boolean existsByUser(User user);
}

