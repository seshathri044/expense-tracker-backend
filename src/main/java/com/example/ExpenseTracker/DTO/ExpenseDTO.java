package com.example.ExpenseTracker.DTO;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ExpenseDTO {
    private Long id;

    // 🔥 CRITICAL FIX: Changed from Integer to Double
    private Double amount;

    private String category;
    private String description;
    private LocalDate date;
    private String notes;
}