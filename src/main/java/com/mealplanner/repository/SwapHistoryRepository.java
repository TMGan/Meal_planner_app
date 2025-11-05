package com.mealplanner.repository;

import com.mealplanner.model.SwapHistory;
import com.mealplanner.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SwapHistoryRepository extends JpaRepository<SwapHistory, Long> {
    List<SwapHistory> findByUserOrderBySwapDateDesc(User user);
    List<SwapHistory> findByUserAndOriginalFood(User user, String originalFood);
}

