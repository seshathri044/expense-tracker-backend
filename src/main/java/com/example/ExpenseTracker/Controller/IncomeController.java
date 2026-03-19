package com.example.ExpenseTracker.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ExpenseTracker.DTO.IncomeDTO;
import com.example.ExpenseTracker.Entity.Income;
import com.example.ExpenseTracker.Entity.UserEntity;
import com.example.ExpenseTracker.Repository.UserRepository;
import com.example.ExpenseTracker.Service.IncomeService;
import com.example.ExpenseTracker.Util.JwtUtil;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/income")
public class IncomeController {

    private final IncomeService incomeService;
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
    public ResponseEntity<?> postIncome(@RequestBody IncomeDTO incomeDTO, HttpServletRequest request) {
        log.info("Creating new income: {}", incomeDTO.getTitle());
        try {
            // 🔥 CRITICAL: Extract userId from JWT
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Authentication required"));
            }

            Income createdIncome = incomeService.postIncome(incomeDTO, userId);
            if (createdIncome != null) {
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(createSuccessResponse("Income added successfully", createdIncome));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Failed to create income"));
            }
        } catch (Exception e) {
            log.error("Error creating income: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to create income"));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllIncomes(HttpServletRequest request) {
        log.info("Fetching all incomes");
        try {
            // 🔥 CRITICAL: Extract userId from JWT
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Authentication required"));
            }

            List<IncomeDTO> incomes = incomeService.getAllIncomes(userId);
            return ResponseEntity.ok(createSuccessResponse("Incomes fetched successfully", incomes));
        } catch (Exception e) {
            log.error("Error fetching incomes: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch incomes"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getIncomeById(@PathVariable Long id, HttpServletRequest request) {
        log.info("Fetching income with id: {}", id);
        try {
            // 🔥 CRITICAL: Extract userId from JWT
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Authentication required"));
            }

            IncomeDTO income = incomeService.getIncomeById(id, userId);
            return ResponseEntity.ok(createSuccessResponse("Income fetched successfully", income));
        } catch (EntityNotFoundException ex) {
            log.warn("Income not found with id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(ex.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching income: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Something went wrong"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateIncome(@PathVariable Long id, @RequestBody IncomeDTO incomeDTO,
                                          HttpServletRequest request) {
        log.info("Updating income with id: {}", id);
        try {
            // 🔥 CRITICAL: Extract userId from JWT
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Authentication required"));
            }

            Income updatedIncome = incomeService.updateIncome(id, incomeDTO, userId);
            return ResponseEntity.ok(createSuccessResponse("Income updated successfully", updatedIncome));
        } catch (EntityNotFoundException ex) {
            log.warn("Income not found with id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(ex.getMessage()));
        } catch (Exception e) {
            log.error("Error updating income: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Something went wrong!"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteIncome(@PathVariable Long id, HttpServletRequest request) {
        log.info("Deleting income with id: {}", id);
        try {
            // 🔥 CRITICAL: Extract userId from JWT
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Authentication required"));
            }

            incomeService.deleteIncome(id, userId);
            return ResponseEntity.ok(createSuccessResponse("Income deleted successfully", null));
        } catch (EntityNotFoundException ex) {
            log.warn("Income not found with id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(ex.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting income: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Something went wrong"));
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