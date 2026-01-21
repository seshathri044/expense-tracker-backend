package com.example.ExpenseTracker.Service;

import com.example.ExpenseTracker.DTO.ExpenseDTO;
import com.example.ExpenseTracker.Entity.Expense;

import java.util.List;

public interface ExpenseService {
    // 🔥 UPDATED: All methods now require userId
    Expense postExpense(ExpenseDTO expenseDTO, Long userId);

    List<Expense> getAllExpenses(Long userId);

    Expense getExpenseById(Long id, Long userId);

    Expense updateExpense(Long id, ExpenseDTO expenseDTO, Long userId);

    void deleteExpense(Long id, Long userId);
}