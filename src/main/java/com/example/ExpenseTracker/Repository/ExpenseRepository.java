package com.example.ExpenseTracker.Repository;

import com.example.ExpenseTracker.Entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // 🔥 NEW: Find all expenses for specific user
    List<Expense> findByUserId(Long userId);

    // 🔥 NEW: Find expense by ID and userId (ensures user owns it)
    Optional<Expense> findByIdAndUserId(Long id, Long userId);

    // 🔥 UPDATED: Filter by userId
    List<Expense> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    // 🔥 UPDATED: Sum only user's expenses
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.userId = :userId")
    Double sumAllAmountsByUserId(@Param("userId") Long userId);

    // 🔥 UPDATED: Get user's most recent expense
    Optional<Expense> findFirstByUserIdOrderByDateDesc(Long userId);

    // 🔥 KEEP: Original methods (for backward compatibility, but won't be used)
    List<Expense> findByDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT SUM(e.amount) FROM Expense e")
    Double sumAllAmounts();

    Optional<Expense> findFirstByOrderByDateDesc();
}