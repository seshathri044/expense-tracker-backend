package com.example.ExpenseTracker.Controller;

import com.example.ExpenseTracker.IO.ProfileRequest;
import com.example.ExpenseTracker.IO.ProfileResponse;
import com.example.ExpenseTracker.Service.EmailService;
import com.example.ExpenseTracker.Service.ProfileService;
import jakarta.validation.Valid;
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
    private final EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody ProfileRequest request) {
        try {
            log.info("Registration request received for email: {}", request.getEmail());

            ProfileResponse response = profileService.createProfile(request);
            log.info("Profile created successfully for: {}", response.getEmail());

            // Send welcome email (don't let email failure break registration)
            try {
                emailService.sendWelcomeEmail(response.getEmail(), response.getName());
                log.info("Welcome email sent successfully to: {}", response.getEmail());
            } catch (Exception emailEx) {
                log.error("Failed to send welcome email to {}: {}", response.getEmail(), emailEx.getMessage());
            }

            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("error", false);
            successResponse.put("message", "Registration successful");
            successResponse.put("data", response);

            return ResponseEntity.status(HttpStatus.CREATED).body(successResponse);

        } catch (Exception e) {
            log.error("Registration failed for email {}: {}", request.getEmail(), e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", true);
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@CurrentSecurityContext(expression = "authentication?.name") String email) {
        try {
            if (email == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", true);
                errorResponse.put("message", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

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