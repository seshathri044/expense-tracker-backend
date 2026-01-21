package com.example.ExpenseTracker.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "expense")
@Data
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔥 CRITICAL FIX: Changed from Integer to Double
    private Double amount;

    private String category;
    private String description;
    private LocalDate date;
    private String notes;

    // 🔥 NEW: Add userId to link expenses to users
    @Column(name = "user_id")
    private Long userId;

    // 🔥 NEW: Track creation time
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}