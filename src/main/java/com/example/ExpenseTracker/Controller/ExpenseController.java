package com.example.ExpenseTracker.Controller;

import com.example.ExpenseTracker.DTO.ExpenseDTO;
import com.example.ExpenseTracker.Entity.Expense;
import com.example.ExpenseTracker.Entity.UserEntity;
import com.example.ExpenseTracker.Repository.UserRepository;
import com.example.ExpenseTracker.Service.ExpenseService;
import com.example.ExpenseTracker.Util.JwtUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/expense")
@CrossOrigin(origins = "*")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    // 🔥 Helper method to extract userId from JWT
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String jwt = extractJwtFromRequest(request);

        if (jwt == null) {
            log.error("No JWT token found in request");
            return null;
        }

        try {
            // Try to extract userId directly from token first (new tokens)
            Long userId = jwtUtil.extractUserId(jwt);
            if (userId != null) {
                log.debug("Extracted userId from JWT: {}", userId);
                return userId;
            }

            // Fallback: Extract email and fetch userId from database (old tokens)
            String email = jwtUtil.extractEmail(jwt);
            log.debug("JWT doesn't have userId, fetching from email: {}", email);

            UserEntity user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));

            return user.getId();
        } catch (Exception e) {
            log.error("Error extracting userId from JWT: {}", e.getMessage());
            return null;
        }
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        // Check Authorization header first
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // Check cookies
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    @PostMapping
    public ResponseEntity<?> postExpense(@RequestBody ExpenseDTO dto, HttpServletRequest request) {
        log.info("Creating new expense: {}", dto.getDescription());
        try {
            // 🔥 CRITICAL: Extract userId from JWT
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Authentication required"));
            }

            // Validate required fields
            if (dto.getAmount() == null || dto.getAmount() <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Amount must be greater than 0"));
            }
            if (dto.getCategory() == null || dto.getCategory().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Category is required"));
            }
            if (dto.getDescription() == null || dto.getDescription().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Description is required"));
            }

            Expense createdExpense = expenseService.postExpense(dto, userId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(createSuccessResponse("Expense added successfully", createdExpense));
        } catch (Exception e) {
            log.error("Error creating expense: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to create expense: " + e.getMessage()));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllExpenses(HttpServletRequest request) {
        log.info("Fetching all expenses");
        try {
            // 🔥 CRITICAL: Extract userId from JWT
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Authentication required"));
            }

            List<Expense> expenses = expenseService.getAllExpenses(userId);
            return ResponseEntity.ok(createSuccessResponse("Expenses fetched successfully", expenses));
        } catch (Exception e) {
            log.error("Error fetching expenses: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch expenses"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getExpenseById(@PathVariable Long id, HttpServletRequest request) {
        log.info("Fetching expense with id: {}", id);
        try {
            // 🔥 CRITICAL: Extract userId from JWT
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Authentication required"));
            }

            Expense expense = expenseService.getExpenseById(id, userId);
            return ResponseEntity.ok(createSuccessResponse("Expense fetched successfully", expense));
        } catch (EntityNotFoundException ex) {
            log.warn("Expense not found with id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(ex.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching expense: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Something went wrong"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateExpense(@PathVariable Long id, @RequestBody ExpenseDTO expenseDTO,
                                           HttpServletRequest request) {
        log.info("Updating expense with id: {}", id);
        try {
            // 🔥 CRITICAL: Extract userId from JWT
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Authentication required"));
            }

            Expense updatedExpense = expenseService.updateExpense(id, expenseDTO, userId);
            return ResponseEntity.ok(createSuccessResponse("Expense updated successfully", updatedExpense));
        } catch (EntityNotFoundException ex) {
            log.warn("Expense not found with id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(ex.getMessage()));
        } catch (Exception e) {
            log.error("Error updating expense: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Something went wrong!"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteExpense(@PathVariable Long id, HttpServletRequest request) {
        log.info("Deleting expense with id: {}", id);
        try {
            // 🔥 CRITICAL: Extract userId from JWT
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Authentication required"));
            }

            expenseService.deleteExpense(id, userId);
            return ResponseEntity.ok(createSuccessResponse("Expense deleted successfully", null));
        } catch (EntityNotFoundException ex) {
            log.warn("Expense not found with id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(ex.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting expense: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Something went wrong!"));
        }
    }

    // Helper methods for consistent response format
    private Map<String, Object> createSuccessResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        return response;
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("data", null);
        return response;
    }
}