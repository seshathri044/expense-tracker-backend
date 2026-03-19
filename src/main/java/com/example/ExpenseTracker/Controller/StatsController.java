package com.example.ExpenseTracker.Controller;

import com.example.ExpenseTracker.DTO.GraphDTO;
import com.example.ExpenseTracker.Entity.UserEntity;
import com.example.ExpenseTracker.Repository.UserRepository;
import com.example.ExpenseTracker.Service.Stats.StatsService;
import com.example.ExpenseTracker.Util.JwtUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/stats")
public class StatsController {

    private final StatsService statsService;
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

    @GetMapping("/chart")
    public ResponseEntity<?> getChartDetails(HttpServletRequest request) {
        log.info("Fetching chart data for last 180 days");
        try {
            // 🔥 CRITICAL: Extract userId from JWT
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Authentication required"));
            }

            GraphDTO chartData = statsService.getChartData(userId);
            return ResponseEntity.ok(chartData);
        } catch (Exception e) {
            log.error("Error fetching chart data: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch chart data"));
        }
    }

    @GetMapping("/chart/{days}")
    public ResponseEntity<?> getChartDetailsByDays(@PathVariable int days, HttpServletRequest request) {
        log.info("Fetching chart data for last {} days", days);
        try {
            // 🔥 CRITICAL: Extract userId from JWT
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Authentication required"));
            }

            GraphDTO chartData = statsService.getChartDataByDays(days, userId);
            return ResponseEntity.ok(chartData);
        } catch (Exception e) {
            log.error("Error fetching chart data for {} days: {}", days, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch chart data"));
        }
    }

    @GetMapping
    public ResponseEntity<?> getStats(HttpServletRequest request) {
        log.info("Fetching statistics");
        try {
            // 🔥 CRITICAL: Extract userId from JWT
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Authentication required"));
            }

            return ResponseEntity.ok(statsService.getStats(userId));
        } catch (Exception e) {
            log.error("Error fetching statistics: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch statistics"));
        }
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("data", null);
        return response;
    }
}