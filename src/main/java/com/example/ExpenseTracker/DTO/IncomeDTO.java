package com.example.ExpenseTracker.DTO;

import lombok.Data;

import java.time.LocalDate;
@Data
public class IncomeDTO {
    private Long id;

    private String title;

    private Double amount;

    private LocalDate date;

    private String category;

    private String description;
}
