package com.example.ExpenseTracker.Service;

import com.example.ExpenseTracker.DTO.IncomeDTO;
import com.example.ExpenseTracker.Entity.Income;

import java.util.List;

public interface IncomeService {

    Income postIncome(IncomeDTO incomeDTO);

    List<IncomeDTO> getAllIncomes();

    Income updateIncome(Long id, IncomeDTO incomeDTO);

    IncomeDTO getIncomeById(Long id);

    void deleteIncome(Long id);
}
