package com.example.ExpenseTracker.Controller;

import com.example.ExpenseTracker.DTO.IncomeDTO;
import com.example.ExpenseTracker.Entity.Income;
import com.example.ExpenseTracker.Service.IncomeService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/income")
public class IncomeController {

    private final IncomeService incomeService;

    @PostMapping
    public ResponseEntity<?> postIncome(@RequestBody IncomeDTO incomeDTO) {
        log.info("Creating new income: {}", incomeDTO.getTitle());
        try {
            Income createdIncome = incomeService.postIncome(incomeDTO);
            if (createdIncome != null) {
                return ResponseEntity.status(HttpStatus.CREATED).body(createdIncome);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        } catch (Exception e) {
            log.error("Error creating income: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create income");
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllIncomes() {
        log.info("Fetching all incomes");
        try {
            return ResponseEntity.ok(incomeService.getAllIncomes());
        } catch (Exception e) {
            log.error("Error fetching incomes: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to fetch incomes");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getIncomeById(@PathVariable Long id) {
        log.info("Fetching income with id: {}", id);
        try {
            return ResponseEntity.ok(incomeService.getIncomeById(id));
        } catch (EntityNotFoundException ex) {
            log.warn("Income not found with id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (Exception e) {
            log.error("Error fetching income: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateIncome(@PathVariable Long id, @RequestBody IncomeDTO incomeDTO) {
        log.info("Updating income with id: {}", id);
        try {
            return ResponseEntity.ok(incomeService.updateIncome(id, incomeDTO));
        } catch (EntityNotFoundException ex) {
            log.warn("Income not found with id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (Exception e) {
            log.error("Error updating income: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong!");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteIncome(@PathVariable Long id) {
        log.info("Deleting income with id: {}", id);
        try {
            incomeService.deleteIncome(id);
            return ResponseEntity.ok(null);
        } catch (EntityNotFoundException ex) {
            log.warn("Income not found with id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (Exception e) {
            log.error("Error deleting income: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong");
        }
    }
}