package com.example.ExpenseTracker.DTO;

import com.example.ExpenseTracker.Entity.Expense;
import com.example.ExpenseTracker.Entity.Income;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class StatsDTO {
    private Double income;
    private Double expense;
    private Income latestIncome;
    private Expense latestExpense;
    private Double balance;
    private Double minIncome;
    private Double maxIncome;
    private Double minExpense;
    private Double maxExpense;

    // 🔥 NEW FIELDS: Required by Flutter
    private List<Map<String, Object>> categoryBreakdown;
    private List<Map<String, Object>> monthlyData;
}