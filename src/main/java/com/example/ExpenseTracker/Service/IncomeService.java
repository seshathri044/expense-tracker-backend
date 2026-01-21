package com.example.ExpenseTracker.Service;

import com.example.ExpenseTracker.DTO.IncomeDTO;
import com.example.ExpenseTracker.Entity.Income;

import java.util.List;

public interface IncomeService {
    // 🔥 UPDATED: All methods now require userId
    Income postIncome(IncomeDTO incomeDTO, Long userId);

    List<IncomeDTO> getAllIncomes(Long userId);

    Income updateIncome(Long id, IncomeDTO incomeDTO, Long userId);

    IncomeDTO getIncomeById(Long id, Long userId);

    void deleteIncome(Long id, Long userId);
}