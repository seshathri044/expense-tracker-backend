package com.example.ExpenseTracker.Repository;

import com.example.ExpenseTracker.Entity.Income;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface IncomeRepository extends JpaRepository<Income, Long> {

    // 🔥 NEW: Find all income for specific user
    List<Income> findByUserId(Long userId);

    // 🔥 NEW: Find income by ID and userId (ensures user owns it)
    Optional<Income> findByIdAndUserId(Long id, Long userId);

    // 🔥 UPDATED: Filter by userId
    List<Income> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    // 🔥 UPDATED: Sum only user's income
    @Query("SELECT SUM(i.amount) FROM Income i WHERE i.userId = :userId")
    Double sumAllAmountsByUserId(@Param("userId") Long userId);

    // 🔥 UPDATED: Get user's most recent income
    Optional<Income> findFirstByUserIdOrderByDateDesc(Long userId);

    // 🔥 KEEP: Original methods (for backward compatibility, but won't be used)
    List<Income> findByDateBetween(LocalDate startData, LocalDate endDate);

    @Query("SELECT SUM(i.amount) FROM Income i")
    Double sumAllAmounts();

    Optional<Income> findFirstByOrderByDateDesc();
}