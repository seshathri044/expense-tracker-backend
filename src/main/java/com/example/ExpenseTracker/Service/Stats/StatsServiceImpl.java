package com.example.ExpenseTracker.Service.Stats;

import com.example.ExpenseTracker.DTO.GraphDTO;
import com.example.ExpenseTracker.DTO.StatsDTO;
import com.example.ExpenseTracker.Entity.Expense;
import com.example.ExpenseTracker.Entity.Income;
import com.example.ExpenseTracker.Repository.ExpenseRepository;
import com.example.ExpenseTracker.Repository.IncomeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;

    @Override
    public GraphDTO getChartData(Long userId) {
        return getChartDataByDays(180, userId);
    }

    @Override
    public GraphDTO getChartDataByDays(int days, Long userId) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);

        log.info("Fetching chart data for userId: {} from {} to {}", userId, startDate, endDate);

        GraphDTO graphDTO = new GraphDTO();
        graphDTO.setExpenseList(expenseRepository.findByUserIdAndDateBetween(userId, startDate, endDate));
        graphDTO.setIncomeList(incomeRepository.findByUserIdAndDateBetween(userId, startDate, endDate));

        log.info("Chart data fetched: {} expenses, {} incomes",
                graphDTO.getExpenseList().size(),
                graphDTO.getIncomeList().size());

        return graphDTO;
    }

    @Override
    public StatsDTO getStats(Long userId) {
        log.info("Fetching statistics for userId: {}", userId);

        Double totalIncome = incomeRepository.sumAllAmountsByUserId(userId);
        Double totalExpense = expenseRepository.sumAllAmountsByUserId(userId);

        Optional<Income> optionalIncome = incomeRepository.findFirstByUserIdOrderByDateDesc(userId);
        Optional<Expense> optionalExpense = expenseRepository.findFirstByUserIdOrderByDateDesc(userId);

        StatsDTO statsDTO = new StatsDTO();
        statsDTO.setExpense(totalExpense != null ? totalExpense : 0.0);
        statsDTO.setIncome(totalIncome != null ? totalIncome : 0.0);

        optionalIncome.ifPresent(statsDTO::setLatestIncome);
        optionalExpense.ifPresent(statsDTO::setLatestExpense);

        double income = totalIncome != null ? totalIncome : 0.0;
        double expense = totalExpense != null ? totalExpense : 0.0;
        statsDTO.setBalance(income - expense);

        List<Income> incomeList = incomeRepository.findByUserId(userId);
        List<Expense> expenseList = expenseRepository.findByUserId(userId);

        log.info("Stats calculation: Total Income={}, Total Expense={}, Balance={}",
                income, expense, statsDTO.getBalance());

        OptionalDouble minIncome = incomeList.stream().mapToDouble(Income::getAmount).min();
        OptionalDouble maxIncome = incomeList.stream().mapToDouble(Income::getAmount).max();
        OptionalDouble minExpense = expenseList.stream().mapToDouble(Expense::getAmount).min();
        OptionalDouble maxExpense = expenseList.stream().mapToDouble(Expense::getAmount).max();

        statsDTO.setMaxExpense(maxExpense.isPresent() ? maxExpense.getAsDouble() : null);
        statsDTO.setMinExpense(minExpense.isPresent() ? minExpense.getAsDouble() : null);
        statsDTO.setMaxIncome(maxIncome.isPresent() ? maxIncome.getAsDouble() : null);
        statsDTO.setMinIncome(minIncome.isPresent() ? minIncome.getAsDouble() : null);

        // 🔥 Calculate category breakdown
        statsDTO.setCategoryBreakdown(calculateCategoryBreakdown(expenseList, totalExpense));

        // 🔥 Calculate monthly data
        statsDTO.setMonthlyData(calculateMonthlyData(incomeList, expenseList));

        log.info("Stats fetched successfully for userId: {}", userId);

        return statsDTO;
    }

    // 🔥 Calculate category breakdown
    private List<Map<String, Object>> calculateCategoryBreakdown(List<Expense> expenses, Double totalExpense) {
        if (expenses.isEmpty() || totalExpense == null || totalExpense == 0) {
            return new ArrayList<>();
        }

        Map<String, Double> categoryTotals = expenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.summingDouble(Expense::getAmount)
                ));

        Map<String, Long> categoryCounts = expenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.counting()
                ));

        return categoryTotals.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> categoryData = new HashMap<>();
                    String category = entry.getKey();
                    Double amount = entry.getValue();
                    Long count = categoryCounts.get(category);
                    Double percentage = (amount / totalExpense) * 100;

                    categoryData.put("category", category);
                    categoryData.put("amount", amount);
                    categoryData.put("count", count);
                    categoryData.put("percentage", percentage);

                    return categoryData;
                })
                .sorted((a, b) -> Double.compare((Double) b.get("amount"), (Double) a.get("amount")))
                .collect(Collectors.toList());
    }

    // 🔥 FIXED: Calculate monthly data with SHORT month names
    private List<Map<String, Object>> calculateMonthlyData(List<Income> incomes, List<Expense> expenses) {
        List<Map<String, Object>> monthlyData = new ArrayList<>();

        LocalDate now = LocalDate.now();
        for (int i = 11; i >= 0; i--) {
            LocalDate monthStart = now.minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);

            double monthIncome = incomes.stream()
                    .filter(income -> !income.getDate().isBefore(monthStart) && !income.getDate().isAfter(monthEnd))
                    .mapToDouble(Income::getAmount)
                    .sum();

            double monthExpense = expenses.stream()
                    .filter(expense -> !expense.getDate().isBefore(monthStart) && !expense.getDate().isAfter(monthEnd))
                    .mapToDouble(Expense::getAmount)
                    .sum();

            Map<String, Object> monthData = new HashMap<>();

            // ✅ FIX: Use SHORT month format (JAN, FEB, MAR instead of JANUARY, FEBRUARY, MARCH)
            monthData.put("month", monthStart.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
            monthData.put("income", monthIncome);
            monthData.put("expense", monthExpense);
            monthData.put("savings", monthIncome - monthExpense);

            monthlyData.add(monthData);
        }

        return monthlyData;
    }
}