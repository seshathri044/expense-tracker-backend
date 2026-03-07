package com.example.ExpenseTracker.Service;

import com.example.ExpenseTracker.Entity.UserEntity;
import com.example.ExpenseTracker.IO.ProfileRequest;
import com.example.ExpenseTracker.IO.ProfileResponse;
import com.example.ExpenseTracker.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// ✅ PART 3: ProfileService Unit Tests
// 🔥 Pure Mockito - NO real database, NO email sent, NO Spring context
// 📁 File location: src/test/java/com/example/ExpenseTracker/Service/ProfileServiceImplTest.java

@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ProfileServiceImpl profileService;

    // ✅ Test data constants
    private UserEntity sampleUser;
    private ProfileRequest sampleRequest;
    private final String TEST_EMAIL = "seshathri@test.com";
    private final String TEST_NAME = "Seshathri";
    private final String TEST_PASSWORD = "password123";
    private final String ENCODED_PASSWORD = "$2a$10$encodedPasswordHash";

    @BeforeEach
    void setUp() {
        // ✅ Build a verified user entity (already in database)
        sampleUser = UserEntity.builder()
                .id(1L)
                .userId("uuid-1234-5678")
                .name(TEST_NAME)
                .email(TEST_EMAIL)
                .password(ENCODED_PASSWORD)
                .isAccountVerified(true)
                .verifyOtp(null)
                .verifyOtpExpireAt(0L)
                .resetOtp(null)
                .resetOtpExpiresAt(0L)
                .build();

        // ✅ Build a registration request (what Flutter app sends)
        sampleRequest = new ProfileRequest(TEST_NAME, TEST_EMAIL, TEST_PASSWORD);
    }

    // =========================================================
    // ✅ TEST GROUP 1: initiateRegistration() - Step 1 & 2
    // =========================================================

    @Test
    @DisplayName("✅ Should initiate registration successfully for new email")
    void initiateRegistration_Success() {
        // ARRANGE: Email does not exist in DB yet
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        // Email send - void method, do nothing (no real email)
        doNothing().when(emailService).sendWelcomeEmail(TEST_EMAIL, TEST_NAME);

        // ACT + ASSERT: Should NOT throw any exception
        assertDoesNotThrow(
                () -> profileService.initiateRegistration(sampleRequest),
                "Registration should succeed for new email"
        );

        // VERIFY: DB checked for existing email
        verify(userRepository, times(1)).existsByEmail(TEST_EMAIL);
        // VERIFY: Password was encoded
        verify(passwordEncoder, times(1)).encode(TEST_PASSWORD);
        // VERIFY: Welcome email was sent
        verify(emailService, times(1)).sendWelcomeEmail(TEST_EMAIL, TEST_NAME);
        // VERIFY: User NOT saved to DB yet (only after OTP verification)
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("❌ Should throw exception when email already registered")
    void initiateRegistration_ThrowsException_WhenEmailExists() {
        // ARRANGE: Email already in DB
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(true);

        // ACT + ASSERT
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> profileService.initiateRegistration(sampleRequest),
                "Should throw exception for duplicate email"
        );

        assertTrue(exception.getMessage().contains("already exists"),
                "Message should say email already exists");

        // VERIFY: No email sent, no password encoded, no DB save
        verify(emailService, never()).sendWelcomeEmail(anyString(), anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("❌ Should throw exception when name is empty")
    void initiateRegistration_ThrowsException_WhenNameEmpty() {
        // ARRANGE: Empty name
        sampleRequest = new ProfileRequest("", TEST_EMAIL, TEST_PASSWORD);
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);

        // ACT + ASSERT
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> profileService.initiateRegistration(sampleRequest)
        );

        assertTrue(exception.getMessage().contains("Name is required"),
                "Message should say name is required");
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("❌ Should throw exception when name is null")
    void initiateRegistration_ThrowsException_WhenNameNull() {
        // ARRANGE
        sampleRequest = new ProfileRequest(null, TEST_EMAIL, TEST_PASSWORD);
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);

        // ACT + ASSERT
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> profileService.initiateRegistration(sampleRequest)
        );

        assertTrue(exception.getMessage().contains("Name is required"));
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("❌ Should throw exception when password is too short (less than 6 chars)")
    void initiateRegistration_ThrowsException_WhenPasswordTooShort() {
        // ARRANGE: Password less than 6 characters
        sampleRequest = new ProfileRequest(TEST_NAME, TEST_EMAIL, "abc");
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);

        // ACT + ASSERT
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> profileService.initiateRegistration(sampleRequest)
        );

        assertTrue(exception.getMessage().contains("Password must be at least 6 characters"),
                "Message should mention password length requirement");
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("✅ Should still succeed even if welcome email fails")
    void initiateRegistration_SucceedsEvenIfEmailFails() {
        // ARRANGE: Email service throws exception but registration should NOT fail
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        // Simulate email failure
        doThrow(new RuntimeException("SMTP error")).when(emailService)
                .sendWelcomeEmail(TEST_EMAIL, TEST_NAME);

        // ACT + ASSERT: Registration should STILL succeed (email failure is non-critical)
        assertDoesNotThrow(
                () -> profileService.initiateRegistration(sampleRequest),
                "Registration should succeed even if welcome email fails"
        );

        // VERIFY: Email was attempted
        verify(emailService, times(1)).sendWelcomeEmail(TEST_EMAIL, TEST_NAME);
        // VERIFY: User NOT saved to DB (pending registration in memory)
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    // =========================================================
    // ✅ TEST GROUP 2: sendRegistrationOtp() - Step 3 & 4
    // =========================================================

    @Test
    @DisplayName("✅ Should send OTP successfully after registration initiated")
    void sendRegistrationOtp_Success() {
        // ARRANGE: First initiate registration (puts in pendingRegistrations map)
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        doNothing().when(emailService).sendWelcomeEmail(TEST_EMAIL, TEST_NAME);
        profileService.initiateRegistration(sampleRequest); // Step 1: Register

        // Now mock OTP email send
        doNothing().when(emailService).sendOtpEmail(eq(TEST_EMAIL), anyString());

        // ACT + ASSERT: Sending OTP should succeed
        assertDoesNotThrow(
                () -> profileService.sendRegistrationOtp(TEST_EMAIL),
                "Sending OTP should succeed for pending registration"
        );

        // VERIFY: OTP email was sent once
        verify(emailService, times(1)).sendOtpEmail(eq(TEST_EMAIL), anyString());
    }

    @Test
    @DisplayName("❌ Should throw exception when no pending registration found")
    void sendRegistrationOtp_ThrowsException_WhenNoPendingRegistration() {
        // ARRANGE: No registration initiated - pendingRegistrations map is empty
        // (nothing to set up - map is empty by default)

        // ACT + ASSERT
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> profileService.sendRegistrationOtp("nonexistent@test.com"),
                "Should throw exception when no pending registration"
        );

        assertTrue(exception.getMessage().contains("Please complete registration first"),
                "Message should ask to register first");

        // VERIFY: No OTP email sent
        verify(emailService, never()).sendOtpEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("❌ Should throw exception when OTP email fails to send")
    void sendRegistrationOtp_ThrowsException_WhenEmailFails() {
        // ARRANGE: Initiate registration first
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        doNothing().when(emailService).sendWelcomeEmail(TEST_EMAIL, TEST_NAME);
        profileService.initiateRegistration(sampleRequest);

        // Simulate OTP email failure
        doThrow(new RuntimeException("SMTP connection failed"))
                .when(emailService).sendOtpEmail(eq(TEST_EMAIL), anyString());

        // ACT + ASSERT: Should throw because OTP email is CRITICAL (unlike welcome email)
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> profileService.sendRegistrationOtp(TEST_EMAIL)
        );

        assertTrue(exception.getMessage().contains("Failed to send OTP email"),
                "Message should say OTP email failed");
    }

    // =========================================================
    // ✅ TEST GROUP 3: verifyRegistrationOtp() - Step 5 & 6
    // =========================================================

    @Test
    @DisplayName("✅ Should verify OTP and save user to database")
    void verifyRegistrationOtp_Success() {
        // ARRANGE: Full flow - register, then send OTP
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        doNothing().when(emailService).sendWelcomeEmail(TEST_EMAIL, TEST_NAME);
        profileService.initiateRegistration(sampleRequest);

        doNothing().when(emailService).sendOtpEmail(eq(TEST_EMAIL), anyString());
        profileService.sendRegistrationOtp(TEST_EMAIL);

        // Capture the OTP that was set in pendingRegistrations
        // We need to get the OTP from the internal map - use ArgumentCaptor
        org.mockito.ArgumentCaptor<String> otpCaptor =
                org.mockito.ArgumentCaptor.forClass(String.class);
        verify(emailService).sendOtpEmail(eq(TEST_EMAIL), otpCaptor.capture());
        String capturedOtp = otpCaptor.getValue();

        // Mock: save user to DB
        when(userRepository.save(any(UserEntity.class))).thenReturn(sampleUser);

        // ACT + ASSERT: Verifying with correct OTP should succeed
        assertDoesNotThrow(
                () -> profileService.verifyRegistrationOtp(TEST_EMAIL, capturedOtp),
                "OTP verification should succeed with correct OTP"
        );

        // VERIFY: User was saved to DB exactly once
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("❌ Should throw exception when OTP is wrong")
    void verifyRegistrationOtp_ThrowsException_WhenOtpWrong() {
        // ARRANGE: Register and send OTP
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        doNothing().when(emailService).sendWelcomeEmail(TEST_EMAIL, TEST_NAME);
        profileService.initiateRegistration(sampleRequest);

        doNothing().when(emailService).sendOtpEmail(eq(TEST_EMAIL), anyString());
        profileService.sendRegistrationOtp(TEST_EMAIL);

        // ACT + ASSERT: Wrong OTP
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> profileService.verifyRegistrationOtp(TEST_EMAIL, "000000"),
                "Should throw exception for wrong OTP"
        );

        assertTrue(exception.getMessage().contains("Invalid OTP"),
                "Message should say OTP is invalid");

        // VERIFY: User NOT saved to DB
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("❌ Should throw exception when OTP not sent yet")
    void verifyRegistrationOtp_ThrowsException_WhenOtpNotSent() {
        // ARRANGE: Register but DON'T send OTP
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        doNothing().when(emailService).sendWelcomeEmail(TEST_EMAIL, TEST_NAME);
        profileService.initiateRegistration(sampleRequest);

        // ACT + ASSERT: Try to verify without sending OTP first
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> profileService.verifyRegistrationOtp(TEST_EMAIL, "123456"),
                "Should throw exception when OTP was never sent"
        );

        assertTrue(exception.getMessage().contains("OTP not sent"),
                "Message should say OTP not sent");
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("❌ Should throw exception when no pending registration exists")
    void verifyRegistrationOtp_ThrowsException_WhenNoPendingRegistration() {
        // ARRANGE: No pending registration in memory at all

        // ACT + ASSERT
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> profileService.verifyRegistrationOtp("ghost@test.com", "123456")
        );

        assertTrue(exception.getMessage().contains("Registration session not found"),
                "Message should say session not found");
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    // =========================================================
    // ✅ TEST GROUP 4: getProfile() - Fetch user profile
    // =========================================================

    @Test
    @DisplayName("✅ Should return ProfileResponse for valid email")
    void getProfile_Success() {
        // ARRANGE
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(sampleUser));

        // ACT
        ProfileResponse result = profileService.getProfile(TEST_EMAIL);

        // ASSERT
        assertNotNull(result, "ProfileResponse should not be null");
        assertEquals(TEST_EMAIL, result.getEmail(), "Email should match");
        assertEquals(TEST_NAME, result.getName(), "Name should match");
        assertTrue(result.isAccountVerified(), "Account should be verified");

        verify(userRepository, times(1)).findByEmail(TEST_EMAIL);
    }

    @Test
    @DisplayName("❌ Should throw UsernameNotFoundException when user not found")
    void getProfile_ThrowsException_WhenUserNotFound() {
        // ARRANGE
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThrows(
                UsernameNotFoundException.class,
                () -> profileService.getProfile(TEST_EMAIL),
                "Should throw UsernameNotFoundException"
        );

        verify(userRepository, times(1)).findByEmail(TEST_EMAIL);
    }

    // =========================================================
    // ✅ TEST GROUP 5: sendResetOtp() - Password reset step 1
    // =========================================================

    @Test
    @DisplayName("✅ Should send reset OTP to existing user")
    void sendResetOtp_Success() {
        // ARRANGE
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(sampleUser);
        doNothing().when(emailService).sendResetOtpEmail(eq(TEST_EMAIL), anyString());

        // ACT + ASSERT
        assertDoesNotThrow(
                () -> profileService.sendResetOtp(TEST_EMAIL),
                "Send reset OTP should succeed"
        );

        // VERIFY: OTP was saved to DB + email was sent
        verify(userRepository, times(1)).save(any(UserEntity.class));
        verify(emailService, times(1)).sendResetOtpEmail(eq(TEST_EMAIL), anyString());
    }

    @Test
    @DisplayName("❌ Should throw exception when sending reset OTP to non-existent user")
    void sendResetOtp_ThrowsException_WhenUserNotFound() {
        // ARRANGE
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThrows(
                UsernameNotFoundException.class,
                () -> profileService.sendResetOtp(TEST_EMAIL)
        );

        // VERIFY: No save, no email
        verify(userRepository, never()).save(any(UserEntity.class));
        verify(emailService, never()).sendResetOtpEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("❌ Should throw exception when reset OTP email fails")
    void sendResetOtp_ThrowsException_WhenEmailFails() {
        // ARRANGE
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(sampleUser);
        doThrow(new RuntimeException("Email failed"))
                .when(emailService).sendResetOtpEmail(eq(TEST_EMAIL), anyString());

        // ACT + ASSERT
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> profileService.sendResetOtp(TEST_EMAIL)
        );

        assertTrue(exception.getMessage().contains("Unable to send the email"),
                "Message should say email failed");
    }

    // =========================================================
    // ✅ TEST GROUP 6: resetPassword() - Password reset step 2
    // =========================================================

    @Test
    @DisplayName("✅ Should reset password successfully with valid OTP")
    void resetPassword_Success() {
        // ARRANGE: User has a valid reset OTP stored
        String validOtp = "654321";
        long futureExpiry = System.currentTimeMillis() + (10 * 60 * 1000); // 10 min future

        UserEntity userWithOtp = UserEntity.builder()
                .id(1L)
                .userId("uuid-1234")
                .name(TEST_NAME)
                .email(TEST_EMAIL)
                .password(ENCODED_PASSWORD)
                .isAccountVerified(true)
                .resetOtp(validOtp)
                .resetOtpExpiresAt(futureExpiry)
                .verifyOtpExpireAt(0L)
                .build();

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(userWithOtp));
        when(passwordEncoder.encode("newPassword123")).thenReturn("$2a$10$newEncodedPassword");
        when(userRepository.save(any(UserEntity.class))).thenReturn(userWithOtp);

        // ACT + ASSERT
        assertDoesNotThrow(
                () -> profileService.resetPassword(TEST_EMAIL, validOtp, "newPassword123"),
                "Password reset should succeed with valid OTP"
        );

        // VERIFY: Password was encoded and user was saved
        verify(passwordEncoder, times(1)).encode("newPassword123");
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("❌ Should throw exception when reset OTP is wrong")
    void resetPassword_ThrowsException_WhenOtpWrong() {
        // ARRANGE
        UserEntity userWithOtp = UserEntity.builder()
                .id(1L)
                .userId("uuid-1234")
                .name(TEST_NAME)
                .email(TEST_EMAIL)
                .password(ENCODED_PASSWORD)
                .isAccountVerified(true)
                .resetOtp("999999") // correct OTP in DB
                .resetOtpExpiresAt(System.currentTimeMillis() + 600000)
                .verifyOtpExpireAt(0L)
                .build();

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(userWithOtp));

        // ACT + ASSERT: Wrong OTP provided
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> profileService.resetPassword(TEST_EMAIL, "000000", "newPassword123"),
                "Should throw exception for wrong OTP"
        );

        assertTrue(exception.getMessage().contains("Invalid OTP"),
                "Message should say OTP is invalid");

        // VERIFY: Password NOT changed
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("❌ Should throw exception when reset OTP has expired")
    void resetPassword_ThrowsException_WhenOtpExpired() {
        // ARRANGE: OTP expired (set in the past)
        long pastExpiry = System.currentTimeMillis() - 1000; // 1 second ago

        UserEntity userWithExpiredOtp = UserEntity.builder()
                .id(1L)
                .userId("uuid-1234")
                .name(TEST_NAME)
                .email(TEST_EMAIL)
                .password(ENCODED_PASSWORD)
                .isAccountVerified(true)
                .resetOtp("123456")
                .resetOtpExpiresAt(pastExpiry) // EXPIRED
                .verifyOtpExpireAt(0L)
                .build();

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(userWithExpiredOtp));

        // ACT + ASSERT
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> profileService.resetPassword(TEST_EMAIL, "123456", "newPassword123")
        );

        assertTrue(exception.getMessage().contains("OTP has expired"),
                "Message should say OTP expired");

        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("❌ Should throw exception when new password is too short")
    void resetPassword_ThrowsException_WhenNewPasswordTooShort() {
        // ARRANGE: Valid OTP but password too short
        String validOtp = "123456";
        UserEntity userWithOtp = UserEntity.builder()
                .id(1L)
                .userId("uuid-1234")
                .name(TEST_NAME)
                .email(TEST_EMAIL)
                .password(ENCODED_PASSWORD)
                .isAccountVerified(true)
                .resetOtp(validOtp)
                .resetOtpExpiresAt(System.currentTimeMillis() + 600000)
                .verifyOtpExpireAt(0L)
                .build();

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(userWithOtp));

        // ACT + ASSERT: Password "abc" is less than 6 chars
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> profileService.resetPassword(TEST_EMAIL, validOtp, "abc")
        );

        assertTrue(exception.getMessage().contains("Password must be at least 6 characters"),
                "Message should mention password length");
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("❌ Should throw exception when no reset OTP exists")
    void resetPassword_ThrowsException_WhenNoOtpExists() {
        // ARRANGE: User never requested OTP (resetOtp is null)
        UserEntity userNoOtp = UserEntity.builder()
                .id(1L)
                .userId("uuid-1234")
                .name(TEST_NAME)
                .email(TEST_EMAIL)
                .password(ENCODED_PASSWORD)
                .isAccountVerified(true)
                .resetOtp(null) // No OTP requested
                .resetOtpExpiresAt(0L)
                .verifyOtpExpireAt(0L)
                .build();

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(userNoOtp));

        // ACT + ASSERT
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> profileService.resetPassword(TEST_EMAIL, "123456", "newPassword123")
        );

        assertTrue(exception.getMessage().contains("No OTP found"),
                "Message should say no OTP found");
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("❌ Should throw exception when resetting password for non-existent user")
    void resetPassword_ThrowsException_WhenUserNotFound() {
        // ARRANGE
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThrows(
                UsernameNotFoundException.class,
                () -> profileService.resetPassword(TEST_EMAIL, "123456", "newPassword123")
        );

        verify(userRepository, never()).save(any(UserEntity.class));
    }
}