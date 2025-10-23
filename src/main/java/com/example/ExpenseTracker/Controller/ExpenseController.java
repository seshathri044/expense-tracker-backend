package com.example.ExpenseTracker.Controller;

import com.example.ExpenseTracker.DTO.ExpenseDTO;
import com.example.ExpenseTracker.Entity.Expense;
import com.example.ExpenseTracker.Service.ExpenseService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/expense")
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<?> postExpense(@RequestBody ExpenseDTO dto) {
        log.info("Creating new expense: {}", dto.getTitle());
        try {
            Expense createdExpense = expenseService.postExpense(dto);
            if (createdExpense != null) {
                return ResponseEntity.status(HttpStatus.CREATED).body(createdExpense);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        } catch (Exception e) {
            log.error("Error creating expense: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create expense");
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllExpenses() {
        log.info("Fetching all expenses");
        try {
            return ResponseEntity.ok(expenseService.getAllExpenses());
        } catch (Exception e) {
            log.error("Error fetching expenses: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to fetch expenses");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getExpenseById(@PathVariable Long id) {
        log.info("Fetching expense with id: {}", id);
        try {
            return ResponseEntity.ok(expenseService.getExpenseById(id));
        } catch (EntityNotFoundException ex) {
            log.warn("Expense not found with id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (Exception e) {
            log.error("Error fetching expense: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateExpense(@PathVariable Long id, @RequestBody ExpenseDTO expenseDTO) {
        log.info("Updating expense with id: {}", id);
        try {
            return ResponseEntity.ok(expenseService.updateExpense(id, expenseDTO));
        } catch (EntityNotFoundException ex) {
            log.warn("Expense not found with id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (Exception e) {
            log.error("Error updating expense: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong!");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteExpense(@PathVariable Long id) {
        log.info("Deleting expense with id: {}", id);
        try {
            expenseService.deleteExpense(id);
            return ResponseEntity.ok(null);
        } catch (EntityNotFoundException ex) {
            log.warn("Expense not found with id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (Exception e) {
            log.error("Error deleting expense: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong!");
        }
    }
}