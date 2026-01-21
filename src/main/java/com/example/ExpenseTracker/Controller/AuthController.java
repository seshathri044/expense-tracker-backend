package com.example.ExpenseTracker.Controller;

import com.example.ExpenseTracker.Entity.UserEntity;
import com.example.ExpenseTracker.IO.AuthRequest;
import com.example.ExpenseTracker.IO.AuthResponse;
import com.example.ExpenseTracker.IO.ProfileRequest;
import com.example.ExpenseTracker.IO.ResetPasswordRequest;
import com.example.ExpenseTracker.Repository.UserRepository;
import com.example.ExpenseTracker.Service.AppUserDetialsService;
import com.example.ExpenseTracker.Service.ProfileService;
import com.example.ExpenseTracker.Util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AppUserDetialsService appUserDetialsService;
    private final JwtUtil jwtUtil;
    private final ProfileService profileService;
    private final UserRepository userRepository;

    /**
     * ✅ STEP 1 & 2: Register endpoint - stores user temporarily + sends WELCOME email
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody ProfileRequest request) {
        log.info("📝 Registration request received for email: {}", request.getEmail());

        try {
            // Store user temporarily (in-memory) and send welcome email
            profileService.initiateRegistration(request);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Registration successful! Welcome email sent. Please request OTP to verify.");
            response.put("email", request.getEmail());

            log.info("✅ Registration initiated successfully for: {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            log.error("❌ Registration failed for: {}. Error: {}", request.getEmail(), e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

        } catch (Exception e) {
            log.error("❌ Unexpected error during registration for: {}", request.getEmail(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Registration failed. Please try again.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * ✅ STEP 3 & 4: Send OTP for email verification during registration
     */
    @PostMapping("/send-otp")
    public ResponseEntity<Map<String, Object>> sendVerificationOtp(
            @RequestParam(required = false) String email,
            @RequestBody(required = false) Map<String, String> requestBody) {

        // ✅ Get email from either query param OR request body
        String emailToUse = email;
        if (emailToUse == null || emailToUse.trim().isEmpty()) {
            if (requestBody != null && requestBody.containsKey("email")) {
                emailToUse = requestBody.get("email");
            }
        }

        // Validate email is provided
        if (emailToUse == null || emailToUse.trim().isEmpty()) {
            log.warn("⚠️ Email missing in send-otp request");
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Email is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        log.info("📧 Send verification OTP request for email: {}", emailToUse);

        try {
            // Send OTP for pending registration (not yet in database)
            profileService.sendRegistrationOtp(emailToUse);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "OTP sent successfully to your email");
            log.info("✅ Verification OTP sent successfully to: {}", emailToUse);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("❌ Failed to send verification OTP to: {}. Error: {}", emailToUse, e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

        } catch (Exception e) {
            log.error("❌ Unexpected error sending verification OTP to: {}", emailToUse, e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to send OTP. Please try again.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * ✅ STEP 5 & 6: Verify OTP and complete registration (save to database)
     * 🔥 FIXED: Now includes user name in JWT token
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, Object>> verifyOtp(@RequestBody Map<String, Object> request) {
        log.info("🔍 OTP verification request received");

        // Validate request body
        if (request.get("email") == null || request.get("email").toString().trim().isEmpty()) {
            log.warn("⚠️ Email missing in verify-otp request");
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Email is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        if (request.get("otp") == null || request.get("otp").toString().trim().isEmpty()) {
            log.warn("⚠️ OTP missing in verify-otp request");
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "OTP is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        String email = request.get("email").toString();
        String otp = request.get("otp").toString();

        log.info("🔍 Verifying OTP for email: {}", email);

        try {
            // Verify OTP and complete registration (saves to database)
            profileService.verifyRegistrationOtp(email, otp);

            // 🔥 CRITICAL FIX: Get user from database and include userId AND name in JWT
            UserEntity user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found after registration"));

            final UserDetails userDetails = appUserDetialsService.loadUserByUsername(email);

            // 🔥 CRITICAL FIX: Pass userId AND name to JWT token
            final String jwtToken = jwtUtil.generateToken(userDetails, user.getId(), user.getName());

            ResponseCookie cookie = ResponseCookie.from("jwt", jwtToken)
                    .httpOnly(true)
                    .path("/")
                    .maxAge(Duration.ofDays(1))
                    .sameSite("Strict")
                    .build();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Account verified successfully! Welcome aboard!");
            response.put("token", jwtToken);
            response.put("email", email);

            log.info("✅ 🎉 OTP verified and account created successfully for: {} (Name: {})", email, user.getName());

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(response);

        } catch (RuntimeException e) {
            log.error("❌ OTP verification failed for: {}. Error: {}", email, e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

        } catch (Exception e) {
            log.error("❌ Unexpected error during OTP verification for: {}", email, e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Verification failed. Please try again.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * ✅ LOGIN endpoint
     * 🔥 FIXED: Now includes user name in JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            log.info("🔐 Login attempt for email: {}", request.getEmail());

            authenticate(request.getEmail(), request.getPassword());

            // 🔥 CRITICAL FIX: Get user from database to include userId AND name in JWT
            UserEntity user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            final UserDetails userDetails = appUserDetialsService.loadUserByUsername(request.getEmail());

            // 🔥 CRITICAL FIX: Pass userId AND name to JWT token
            final String jwtToken = jwtUtil.generateToken(userDetails, user.getId(), user.getName());

            ResponseCookie cookie = ResponseCookie.from("jwt", jwtToken)
                    .httpOnly(true)
                    .path("/")
                    .maxAge(Duration.ofDays(1))
                    .sameSite("Strict")
                    .build();

            log.info("✅ Login successful for email: {} with userId: {} (Name: {})",
                    request.getEmail(), user.getId(), user.getName());

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(new AuthResponse(request.getEmail(), jwtToken));

        } catch (BadCredentialsException ex) {
            log.warn("⚠️ Login failed - Invalid credentials for email: {}", request.getEmail());
            Map<String, Object> error = new HashMap<>();
            error.put("error", true);
            error.put("message", "Email or password is incorrect");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

        } catch (DisabledException ex) {
            log.warn("⚠️ Login failed - Account disabled for email: {}", request.getEmail());
            Map<String, Object> error = new HashMap<>();
            error.put("error", true);
            error.put("message", "Account is disabled");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);

        } catch (Exception ex) {
            log.error("❌ Login failed for email: {}", request.getEmail(), ex);
            Map<String, Object> error = new HashMap<>();
            error.put("error", true);
            error.put("message", "Authentication failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    private void authenticate(String email, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
    }

    @GetMapping("/is-authenticated")
    public ResponseEntity<Map<String, Object>> isAuthenticated(
            @CurrentSecurityContext(expression = "authentication?.name") String email) {

        Map<String, Object> response = new HashMap<>();
        response.put("authenticated", email != null);
        if (email != null) {
            response.put("email", email);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/send-reset-otp")
    public ResponseEntity<Map<String, Object>> sendResetOtp(
            @RequestParam(required = false) String email,
            @RequestBody(required = false) Map<String, String> requestBody) {

        // ✅ Get email from either query param OR request body
        String emailToUse = email;
        if (emailToUse == null || emailToUse.trim().isEmpty()) {
            if (requestBody != null && requestBody.containsKey("email")) {
                emailToUse = requestBody.get("email");
            }
        }

        // Validate email is provided
        if (emailToUse == null || emailToUse.trim().isEmpty()) {
            log.warn("⚠️ Email missing in send-reset-otp request");
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Email is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        log.info("🔑 Reset OTP request received for email: {}", emailToUse);

        try {
            profileService.sendResetOtp(emailToUse);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Password reset OTP sent successfully to your email");
            log.info("✅ Reset OTP sent successfully to: {}", emailToUse);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Failed to send reset OTP to: {}", emailToUse, e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        log.info("🔑 Password reset request received for email: {}", request.getEmail());

        try {
            profileService.resetPassword(
                    request.getEmail(),
                    request.getOtp(),
                    request.getNewPassword()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Password reset successfully");
            log.info("✅ Password reset successfully for: {}", request.getEmail());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Failed to reset password for: {}", request.getEmail(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        log.info("👋 Logout request received");

        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Logged out successfully");

        log.info("✅ Logout successful");

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    /**
     * ✅ TEST ENDPOINT - Remove in production
     */
    @PostMapping("/test-email")
    public ResponseEntity<Map<String, Object>> testEmail(@RequestParam String email) {
        log.info("🧪 Testing email functionality for: {}", email);

        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Check your email service logs");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Email test failed: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Email test failed: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}