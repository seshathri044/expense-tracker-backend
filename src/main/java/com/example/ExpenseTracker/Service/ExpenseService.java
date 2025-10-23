package com.example.ExpenseTracker.Service;


import com.example.ExpenseTracker.DTO.ExpenseDTO;
import com.example.ExpenseTracker.Entity.Expense;

import java.util.List;

public interface ExpenseService {
    Expense postExpense(ExpenseDTO expenseDTO);

    List<Expense> getAllExpenses();

    Expense getExpenseById(Long id);

    Expense updateExpense(Long id ,ExpenseDTO expenseDTO);

    void deleteExpense(Long id);
}
