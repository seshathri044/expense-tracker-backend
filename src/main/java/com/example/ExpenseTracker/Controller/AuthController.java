package com.example.ExpenseTracker.Controller;

import com.example.ExpenseTracker.IO.AuthRequest;
import com.example.ExpenseTracker.IO.AuthResponse;
import com.example.ExpenseTracker.IO.ResetPasswordRequest;
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
import org.springframework.web.server.ResponseStatusException;

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

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            log.info("Login attempt for email: {}", request.getEmail());

            authenticate(request.getEmail(), request.getPassword());
            final UserDetails userDetails = appUserDetialsService.loadUserByUsername(request.getEmail());
            final String jwtToken = jwtUtil.generateToken(userDetails);

            ResponseCookie cookie = ResponseCookie.from("jwt", jwtToken)
                    .httpOnly(true)
                    .path("/")
                    .maxAge(Duration.ofDays(1))
                    .sameSite("Strict")
                    .build();

            log.info("Login successful for email: {}", request.getEmail());

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(new AuthResponse(request.getEmail(), jwtToken));

        } catch (BadCredentialsException ex) {
            log.warn("Login failed - Invalid credentials for email: {}", request.getEmail());
            Map<String, Object> error = new HashMap<>();
            error.put("error", true);
            error.put("message", "Email or password is incorrect");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

        } catch (DisabledException ex) {
            log.warn("Login failed - Account disabled for email: {}", request.getEmail());
            Map<String, Object> error = new HashMap<>();
            error.put("error", true);
            error.put("message", "Account is disabled");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);

        } catch (Exception ex) {
            log.error("Login failed for email: {}", request.getEmail(), ex);
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
    public ResponseEntity<Map<String, Object>> sendResetOtp(@RequestParam String email) {
        log.info("Reset OTP request received for email: {}", email);

        try {
            profileService.sendResetOtp(email);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "OTP sent successfully to your email");
            log.info("Reset OTP sent successfully to: {}", email);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to send reset OTP to: {}", email, e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        log.info("Password reset request received for email: {}", request.getEmail());

        try {
            profileService.resetPassword(
                    request.getEmail(),
                    request.getOtp(),
                    request.getNewPassword()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Password reset successfully");
            log.info("Password reset successfully for: {}", request.getEmail());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to reset password for: {}", request.getEmail(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        log.info("Logout request received");

        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(false)  // Set to true in production with HTTPS
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Logged out successfully");

        log.info("Logout successful");

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    @PostMapping("/send-otp")
    public ResponseEntity<Map<String, Object>> sendVerifyOtp(@RequestParam String email) {
        log.info("Send verification OTP request for email: {}", email);

        try {
            profileService.sendOtp(email);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "OTP sent successfully to your email");
            log.info("Verification OTP sent successfully to: {}", email);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to send verification OTP to: {}", email, e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, Object>> verifyEmail(@RequestBody Map<String, Object> request) {
        log.info("OTP verification request received");

        // Validate request body
        if (request.get("email") == null || request.get("email").toString().trim().isEmpty()) {
            log.warn("Email missing in verify-otp request");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }

        if (request.get("otp") == null || request.get("otp").toString().trim().isEmpty()) {
            log.warn("OTP missing in verify-otp request");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP is required");
        }

        String email = request.get("email").toString();
        String otp = request.get("otp").toString();

        log.info("Verifying OTP for email: {}", email);

        try {
            profileService.verifyOtp(email, otp);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Account verified successfully");
            log.info("Account verified successfully for: {}", email);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("OTP verification failed for: {}. Error: {}", email, e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

        } catch (Exception e) {
            log.error("Unexpected error during OTP verification for: {}", email, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to verify OTP");
        }
    }

}