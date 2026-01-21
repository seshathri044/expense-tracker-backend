package com.example.ExpenseTracker.Service;

import com.example.ExpenseTracker.DTO.ExpenseDTO;
import com.example.ExpenseTracker.Entity.Expense;
import com.example.ExpenseTracker.Repository.ExpenseRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;

    @Override
    public Expense postExpense(ExpenseDTO expenseDTO, Long userId) {
        log.info("Creating expense for userId: {} with amount: {}", userId, expenseDTO.getAmount());
        Expense expense = new Expense();
        expense.setUserId(userId); // 🔥 CRITICAL: Set userId
        return saveOrUpdateExpense(expense, expenseDTO);
    }

    private Expense saveOrUpdateExpense(Expense expense, ExpenseDTO expenseDTO) {
        expense.setAmount(expenseDTO.getAmount());
        expense.setCategory(expenseDTO.getCategory());
        expense.setDescription(expenseDTO.getDescription());
        expense.setDate(expenseDTO.getDate());
        expense.setNotes(expenseDTO.getNotes());

        Expense savedExpense = expenseRepository.save(expense);
        log.info("Expense saved with ID: {} for userId: {}", savedExpense.getId(), savedExpense.getUserId());
        return savedExpense;
    }

    @Override
    public List<Expense> getAllExpenses(Long userId) {
        // 🔥 FIXED: Only fetch user's expenses
        List<Expense> expenses = expenseRepository.findByUserId(userId).stream()
                .sorted(Comparator.comparing(Expense::getDate).reversed())
                .collect(Collectors.toList());
        log.info("Fetched {} expenses for userId: {}", expenses.size(), userId);
        return expenses;
    }

    @Override
    public Expense getExpenseById(Long id, Long userId) {
        // 🔥 FIXED: Verify user owns this expense
        Optional<Expense> optionalExpense = expenseRepository.findByIdAndUserId(id, userId);
        if (optionalExpense.isPresent()) {
            return optionalExpense.get();
        } else {
            throw new EntityNotFoundException("Expense not found with id: " + id + " for user: " + userId);
        }
    }

    @Override
    public Expense updateExpense(Long id, ExpenseDTO expenseDTO, Long userId) {
        // 🔥 FIXED: Verify user owns this expense before updating
        Optional<Expense> optionalExpense = expenseRepository.findByIdAndUserId(id, userId);
        if (optionalExpense.isPresent()) {
            return saveOrUpdateExpense(optionalExpense.get(), expenseDTO);
        } else {
            throw new EntityNotFoundException("Expense not found with id: " + id + " for user: " + userId);
        }
    }

    @Override
    public void deleteExpense(Long id, Long userId) {
        // 🔥 FIXED: Verify user owns this expense before deleting
        Optional<Expense> optionalExpense = expenseRepository.findByIdAndUserId(id, userId);
        if (optionalExpense.isPresent()) {
            expenseRepository.deleteById(id);
            log.info("Expense deleted with id: {} by userId: {}", id, userId);
        } else {
            throw new EntityNotFoundException("Expense not found with id: " + id + " for user: " + userId);
        }
    }
}