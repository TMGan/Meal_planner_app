package com.mealplanner.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_swap_history")
public class SwapHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "swap_type", nullable = false, length = 20)
    private String swapType;

    @Column(name = "original_food", nullable = false)
    private String originalFood;

    @Column(name = "replacement_food", nullable = false)
    private String replacementFood;

    @Column(name = "meal_context", length = 50)
    private String mealContext;

    @Column(name = "swap_date")
    private LocalDateTime swapDate;

    public SwapHistory() {
        this.swapDate = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getSwapType() { return swapType; }
    public void setSwapType(String swapType) { this.swapType = swapType; }
    public String getOriginalFood() { return originalFood; }
    public void setOriginalFood(String originalFood) { this.originalFood = originalFood; }
    public String getReplacementFood() { return replacementFood; }
    public void setReplacementFood(String replacementFood) { this.replacementFood = replacementFood; }
    public String getMealContext() { return mealContext; }
    public void setMealContext(String mealContext) { this.mealContext = mealContext; }
    public LocalDateTime getSwapDate() { return swapDate; }
    public void setSwapDate(LocalDateTime swapDate) { this.swapDate = swapDate; }
}

