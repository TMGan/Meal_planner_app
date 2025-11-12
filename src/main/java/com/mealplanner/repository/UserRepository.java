package com.mealplanner.repository;

import com.mealplanner.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByGoogleId(String googleId);
    boolean existsByEmail(String email);

    // Admin queries
    List<User> findTop10ByOrderByCreatedAtDesc();
    long countByCreatedAtAfter(LocalDateTime date);

    List<User> findByEmailContainingIgnoreCaseOrNameContainingIgnoreCase(String email, String name);
}
