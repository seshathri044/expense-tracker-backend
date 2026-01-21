package com.example.ExpenseTracker.Service;

import com.example.ExpenseTracker.Entity.UserEntity;
import com.example.ExpenseTracker.Entity.PendingRegistration;
import com.example.ExpenseTracker.IO.ProfileRequest;
import com.example.ExpenseTracker.IO.ProfileResponse;
import com.example.ExpenseTracker.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    // ✅ IN-MEMORY STORAGE - Users NOT saved to DB until OTP verified
    private final Map<String, PendingRegistration> pendingRegistrations = new ConcurrentHashMap<>();

    /**
     * ✅ STEP 1 & 2: Register user + Send WELCOME EMAIL ONLY (NO OTP YET)
     */
    @Override
    public void initiateRegistration(ProfileRequest request) {
        log.info("🚀 Initiating registration for email: {}", request.getEmail());

        String email = request.getEmail();
        String name = request.getName();

        // Check if email is already registered (verified user in database)
        if (userRepository.existsByEmail(email)) {
            log.warn("⚠️ Email already registered: {}", email);
            throw new RuntimeException("Email already exists. Please login instead.");
        }

        // Validate input
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("Name is required");
        }

        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters");
        }

        // Create pending registration (in-memory only) - NO OTP YET
        PendingRegistration pending = PendingRegistration.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(request.getPassword())) // Pre-encode password
                .otp(null) // ⭐ NO OTP YET - will be set when user clicks "Send OTP"
                .otpExpiresAt(0L)
                .createdAt(System.currentTimeMillis())
                .build();

        // Store in memory (NOT in database)
        pendingRegistrations.put(email, pending);
        log.info("✅ Registration data stored in memory for: {}", email);

        // ✅ STEP 2: Send WELCOME email ONLY (NO OTP)
        try {
            emailService.sendWelcomeEmail(email, name);
            log.info("✅ ✉️ Welcome email sent successfully to: {}", email);
        } catch (Exception e) {
            log.error("⚠️ Failed to send welcome email to: {} - Error: {}", email, e.getMessage());
            // Don't fail registration if welcome email fails
        }

        log.info("🎉 Registration initiated - Welcome email sent to: {}", email);
    }

    /**
     * ✅ STEP 3 & 4: User clicks "Send OTP" button → Generate and send OTP
     */
    @Override
    public void sendRegistrationOtp(String email) {
        log.info("📧 Sending registration OTP to: {}", email);

        // Get pending registration from memory
        PendingRegistration pending = pendingRegistrations.get(email);

        if (pending == null) {
            log.warn("⚠️ No pending registration found for email: {}", email);
            throw new RuntimeException("Please complete registration first");
        }

        // Check if pending registration has expired (1 hour timeout)
        if (pending.isExpired()) {
            log.warn("⏰ Pending registration expired for: {}", email);
            pendingRegistrations.remove(email);
            throw new RuntimeException("Registration session expired. Please register again.");
        }

        // Generate new 6-digit OTP
        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));

        // Set OTP expiry (10 minutes)
        long expiryTime = System.currentTimeMillis() + (10 * 60 * 1000);

        // Update pending registration with new OTP
        pending.setOtp(otp);
        pending.setOtpExpiresAt(expiryTime);

        log.info("🔐 OTP generated for pending registration: {}", email);

        // ✅ STEP 4: Send OTP email
        try {
            emailService.sendOtpEmail(email, otp);
            log.info("✅ ✉️ OTP verification email sent successfully to: {}", email);
        } catch (Exception e) {
            log.error("❌ Failed to send OTP email to: {}", email, e);
            throw new RuntimeException("Failed to send OTP email. Please try again.");
        }
    }

    /**
     * ✅ STEP 5 & 6: Verify OTP and complete registration (save to database)
     */
    @Override
    public void verifyRegistrationOtp(String email, String otp) {
        log.info("🔍 Verifying registration OTP for: {}", email);

        // Get pending registration from memory
        PendingRegistration pending = pendingRegistrations.get(email);

        if (pending == null) {
            log.warn("⚠️ No pending registration found for: {}", email);
            throw new RuntimeException("Registration session not found. Please register again.");
        }

        // Validate OTP exists
        if (pending.getOtp() == null || pending.getOtp().trim().isEmpty()) {
            log.warn("⚠️ No OTP found for pending registration: {}", email);
            throw new RuntimeException("OTP not sent. Please request OTP first.");
        }

        // Validate OTP matches
        if (!pending.getOtp().equals(otp.trim())) {
            log.warn("❌ Invalid OTP provided for: {}", email);
            throw new RuntimeException("Invalid OTP. Please check and try again.");
        }

        // Validate OTP not expired
        if (pending.isOtpExpired()) {
            log.warn("⏰ OTP expired for: {}", email);
            throw new RuntimeException("OTP has expired. Please request a new OTP.");
        }

        // ✅ STEP 6: OTP VERIFIED - NOW SAVE TO DATABASE
        try {
            UserEntity newUser = UserEntity.builder()
                    .userId(UUID.randomUUID().toString())
                    .name(pending.getName())
                    .email(pending.getEmail())
                    .password(pending.getPassword()) // Already encoded
                    .isAccountVerified(true) // Mark as verified
                    .verifyOtp(null)
                    .verifyOtpExpireAt(0L)
                    .resetOtp(null)
                    .resetOtpExpiresAt(0L)
                    .build();

            userRepository.save(newUser);
            log.info("✅ 🎉 User registered successfully in database: {}", email);

            // Remove from pending registrations (clean up memory)
            pendingRegistrations.remove(email);
            log.info("🧹 Pending registration cleaned up for: {}", email);

        } catch (Exception e) {
            log.error("❌ Failed to save user to database: {}", email, e);
            throw new RuntimeException("Registration failed. Please try again.");
        }
    }

    @Override
    public ProfileResponse createProfile(ProfileRequest request) {
        log.info("Creating profile for email: {}", request.getEmail());

        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Email already exists: {}", request.getEmail());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        // Create new user entity
        UserEntity newUser = convertToUserEntity(request);

        try {
            // Save user to database
            newUser = userRepository.save(newUser);
            log.info("User created successfully with ID: {}", newUser.getId());

            return convertToProfileResponse(newUser);
        } catch (Exception e) {
            log.error("Error creating user: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create user");
        }
    }

    @Override
    public ProfileResponse getProfile(String email) {
        log.info("Getting profile for email: {}", email);

        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return convertToProfileResponse(existingUser);
    }

    @Override
    public void sendResetOtp(String email) {
        log.info("Sending reset OTP to email: {}", email);

        UserEntity existingEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // Generate 6 digit OTP
        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));

        // Calculate Expiry time (10 minutes)
        long expiryTime = System.currentTimeMillis() + (10 * 60 * 1000);

        // Update the profile entity
        existingEntity.setResetOtp(otp);
        existingEntity.setResetOtpExpiresAt(expiryTime);

        // Save into the database
        userRepository.save(existingEntity);
        log.info("Reset OTP generated and saved for user: {}", email);

        try {
            emailService.sendResetOtpEmail(existingEntity.getEmail(), otp);
            log.info("✅ Reset OTP email sent successfully to: {}", email);
        } catch (Exception ex) {
            log.error("❌ Unable to send reset email to: {}", email, ex);
            throw new RuntimeException("Unable to send the email. Please try again.");
        }
    }

    @Override
    public void resetPassword(String email, String otp, String newPassword) {
        log.info("Resetting password for email: {}", email);

        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // Validate OTP exists
        if (existingUser.getResetOtp() == null || existingUser.getResetOtp().trim().isEmpty()) {
            log.warn("No reset OTP found for user: {}", email);
            throw new RuntimeException("No OTP found. Please request a new OTP.");
        }

        // Validate OTP matches
        if (!existingUser.getResetOtp().equals(otp.trim())) {
            log.warn("Invalid reset OTP provided for user: {}", email);
            throw new RuntimeException("Invalid OTP. Please check and try again.");
        }

        // Validate OTP is not expired
        if (existingUser.getResetOtpExpiresAt() == null ||
                existingUser.getResetOtpExpiresAt() < System.currentTimeMillis()) {
            log.warn("Reset OTP expired for user: {}", email);
            throw new RuntimeException("OTP has expired. Please request a new OTP.");
        }

        // Validate new password
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new RuntimeException("New password cannot be empty");
        }

        if (newPassword.length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters long");
        }

        // Update password and clear OTP data
        existingUser.setPassword(passwordEncoder.encode(newPassword));
        existingUser.setResetOtp(null);
        existingUser.setResetOtpExpiresAt(0L);

        userRepository.save(existingUser);
        log.info("✅ Password reset successfully for user: {}", email);
    }

    /**
     * DEPRECATED: Old sendOtp method - for existing verified users only
     */
    @Override
    @Deprecated
    public void sendOtp(String email) {
        log.info("Sending OTP to existing user: {}", email);

        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        if (existingUser.getIsAccountVerified() != null && existingUser.getIsAccountVerified()) {
            log.info("User already verified: {}", email);
            return;
        }

        // Generate 6 digit OTP
        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
        long expiryTime = System.currentTimeMillis() + (10 * 60 * 1000);

        existingUser.setVerifyOtp(otp);
        existingUser.setVerifyOtpExpireAt(expiryTime);
        userRepository.save(existingUser);

        try {
            emailService.sendOtpEmail(existingUser.getEmail(), otp);
        } catch (Exception e) {
            throw new RuntimeException("Unable to send email");
        }
    }

    /**
     * DEPRECATED: Old verifyOtp method - for existing verified users only
     */
    @Override
    @Deprecated
    public void verifyOtp(String email, String otp) {
        log.info("Verifying OTP for existing user: {}", email);

        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        if (existingUser.getVerifyOtp() == null || !existingUser.getVerifyOtp().equals(otp.trim())) {
            throw new RuntimeException("Invalid OTP");
        }

        if (existingUser.getVerifyOtpExpireAt() < System.currentTimeMillis()) {
            throw new RuntimeException("OTP Expired");
        }

        existingUser.setIsAccountVerified(true);
        existingUser.setVerifyOtp(null);
        existingUser.setVerifyOtpExpireAt(0L);

        userRepository.save(existingUser);
    }

    @Override
    public String getLoggedInUserId(String email) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not Found: " + email));
        return existingUser.getUserId();
    }

    private ProfileResponse convertToProfileResponse(UserEntity user) {
        return ProfileResponse.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .isAccountVerified(user.getIsAccountVerified() != null ? user.getIsAccountVerified() : false)
                .build();
    }

    private UserEntity convertToUserEntity(ProfileRequest request) {
        return UserEntity.builder()
                .userId(UUID.randomUUID().toString())
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .isAccountVerified(false)
                .verifyOtp(null)
                .verifyOtpExpireAt(0L)
                .resetOtp(null)
                .resetOtpExpiresAt(0L)
                .build();
    }
}