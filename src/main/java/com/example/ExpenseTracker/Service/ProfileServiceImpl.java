package com.example.ExpenseTracker.Service;

import com.example.ExpenseTracker.Entity.UserEntity;
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

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

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

        // ✅ FIXED: Changed to 10 minutes to match email message
        // Calculate Expiry time (current time + 10 minutes in milliseconds)
        long expiryTime = System.currentTimeMillis() + (10 * 60 * 1000);

        // Update the profile entity
        existingEntity.setResetOtp(otp);
        existingEntity.setResetOtpExpiresAt(expiryTime);

        // Save into the database
        userRepository.save(existingEntity);
        log.info("OTP generated and saved for user: {}", email);

        try {
            emailService.sendResetOtpEmail(existingEntity.getEmail(), otp);
            log.info("Reset OTP email sent successfully to: {}", email);
        } catch (Exception ex) {
            log.error("Unable to send email to: {}", email, ex);
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
            log.warn("No OTP found for user: {}", email);
            throw new RuntimeException("No OTP found. Please request a new OTP.");
        }

        // Validate OTP matches
        if (!existingUser.getResetOtp().equals(otp)) {
            log.warn("Invalid OTP provided for user: {}", email);
            throw new RuntimeException("Invalid OTP. Please check and try again.");
        }

        // Validate OTP is not expired
        if (existingUser.getResetOtpExpiresAt() == null ||
                existingUser.getResetOtpExpiresAt() < System.currentTimeMillis()) {
            log.warn("OTP expired for user: {}", email);
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
        log.info("Password reset successfully for user: {}", email);
    }

    @Override
    public void sendOtp(String email) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found:"+ email));
        if (existingUser.getIsAccountVerified() != null && existingUser.getIsAccountVerified()){
            return;
        }
        // Generate 6 digit OTP
        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));

        // Calculate Expiry time (current time + 24 hours in milliseconds)
        long expiryTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000);

        // update the user entity
        existingUser.setVerifyOtp(otp);
        existingUser.setVerifyOtpExpireAt(expiryTime);
        // save the database
        userRepository.save(existingUser);

        try{
            emailService.sendOtpEmail(existingUser.getEmail(),otp);
        }catch (Exception e){
            throw new RuntimeException("Unable to send email");
        }
    }

    @Override
    public void verifyOtp(String email, String otp) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found:"+email));
        if(existingUser.getVerifyOtp() == null || !existingUser.getVerifyOtp().equals(otp)){
            throw new RuntimeException("Invalid OTP");
        }
        if(existingUser.getVerifyOtpExpireAt() < System.currentTimeMillis()){
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
                .orElseThrow(() -> new UsernameNotFoundException("User not Found:" + email));
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