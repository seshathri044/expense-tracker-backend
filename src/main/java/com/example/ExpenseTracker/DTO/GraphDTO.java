package com.example.ExpenseTracker.DTO;

import com.example.ExpenseTracker.Entity.Expense;
import com.example.ExpenseTracker.Entity.Income;
import lombok.Data;

import java.util.List;
@Data
public class GraphDTO {

    private List<Expense> expenseList;


    private List<Income> incomeList;
}
