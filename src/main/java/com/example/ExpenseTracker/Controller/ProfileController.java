package com.example.ExpenseTracker.Controller;

import com.example.ExpenseTracker.IO.ProfileResponse;
import com.example.ExpenseTracker.Service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    /**
     * Get user profile - requires authentication
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@CurrentSecurityContext(expression = "authentication?.name") String email) {
        try {
            if (email == null) {
                log.warn("Unauthorized profile access attempt");
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", true);
                errorResponse.put("message", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            log.info("Fetching profile for email: {}", email);
            ProfileResponse response = profileService.getProfile(email);

            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("error", false);
            successResponse.put("data", response);

            return ResponseEntity.ok(successResponse);

        } catch (Exception e) {
            log.error("Failed to get profile for email {}: {}", email, e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", true);
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}