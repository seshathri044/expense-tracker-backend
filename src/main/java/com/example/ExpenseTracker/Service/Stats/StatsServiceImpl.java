package com.example.ExpenseTracker.Service.Stats;

import com.example.ExpenseTracker.DTO.GraphDTO;
import com.example.ExpenseTracker.DTO.StatsDTO;
import com.example.ExpenseTracker.Entity.Expense;
import com.example.ExpenseTracker.Entity.Income;
import com.example.ExpenseTracker.Repository.ExpenseRepository;
import com.example.ExpenseTracker.Repository.IncomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService{

    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;

    @Override
    public GraphDTO getChartData() {
        // Default to 180 days to capture more data
        return getChartDataByDays(180);
    }

    @Override
    public GraphDTO getChartDataByDays(int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);

        GraphDTO graphDTO = new GraphDTO();
        graphDTO.setExpenseList(expenseRepository.findByDateBetween(startDate, endDate));
        graphDTO.setIncomeList(incomeRepository.findByDateBetween(startDate, endDate));

        return graphDTO;
    }

    @Override
    public StatsDTO getStats() {
        Double totalIncome = incomeRepository.sumAllAmounts();
        Double totalExpense = expenseRepository.sumAllAmounts();

        Optional<Income> optionalIncome = incomeRepository.findFirstByOrderByDateDesc();
        Optional<Expense> optionalExpense = expenseRepository.findFirstByOrderByDateDesc();

        StatsDTO statsDTO = new StatsDTO();
        statsDTO.setExpense(totalExpense != null ? totalExpense : 0.0);
        statsDTO.setIncome(totalIncome != null ? totalIncome : 0.0);

        optionalIncome.ifPresent(statsDTO::setLatestIncome);
        optionalExpense.ifPresent(statsDTO::setLatestExpense);

        // Fixed: Handle null values properly for balance calculation
        double income = totalIncome != null ? totalIncome : 0.0;
        double expense = totalExpense != null ? totalExpense : 0.0;
        statsDTO.setBalance(income - expense);

        List<Income> incomeList = incomeRepository.findAll();
        List<Expense> expenseList = expenseRepository.findAll();  // Fixed: Changed from 'expenses' to 'expenseList'

        // Fixed: Integer to double conversion for amount field
        OptionalDouble minIncome = incomeList.stream()
                .mapToDouble(Income::getAmount)
                .min();

        OptionalDouble maxIncome = incomeList.stream()
                .mapToDouble(Income::getAmount)
                .max();

        OptionalDouble minExpense = expenseList.stream()
                .mapToDouble(Expense::getAmount)
                .min();

        OptionalDouble maxExpense = expenseList.stream()
                .mapToDouble(Expense::getAmount)
                .max();

        statsDTO.setMaxExpense(maxExpense.isPresent() ? maxExpense.getAsDouble() : null);
        statsDTO.setMinExpense(minExpense.isPresent() ? minExpense.getAsDouble() : null);

        statsDTO.setMaxIncome(maxIncome.isPresent() ? maxIncome.getAsDouble() : null);
        statsDTO.setMinIncome(minIncome.isPresent() ? minIncome.getAsDouble() : null);

        return statsDTO;
    }

}
