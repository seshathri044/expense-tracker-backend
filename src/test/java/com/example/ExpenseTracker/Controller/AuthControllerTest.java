package com.example.ExpenseTracker.Controller;

import com.example.ExpenseTracker.Entity.UserEntity;
import com.example.ExpenseTracker.IO.ProfileRequest;
import com.example.ExpenseTracker.Repository.UserRepository;
import com.example.ExpenseTracker.Service.ProfileService;
import com.example.ExpenseTracker.Util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// ✅ PART 5b: AuthController Integration Tests
// 🔥 Tests public endpoints: register, login, send-otp, verify-otp, logout
// 📁 File location: src/test/java/com/example/ExpenseTracker/Controller/AuthControllerTest.java

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockBean
    private ProfileService profileService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private AuthenticationManager authenticationManager;

    private ObjectMapper objectMapper;
    private UserEntity sampleUser;

    private final String TEST_EMAIL = "seshathri@test.com";
    private final String TEST_PASSWORD = "password123";
    private final String TEST_NAME = "Seshathri";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        // ✅ Build a verified user entity
        sampleUser = UserEntity.builder()
                .id(1L)
                .userId("uuid-1234")
                .name(TEST_NAME)
                .email(TEST_EMAIL)
                .password("$2a$10$encodedPassword")
                .isAccountVerified(true)
                .verifyOtpExpireAt(0L)
                .resetOtpExpiresAt(0L)
                .build();
    }

    // =========================================================
    // ✅ TEST GROUP 1: POST /register - Registration
    // =========================================================

    @Test
    @DisplayName("✅ POST /register - Should register successfully and return 201")
    void register_Returns201_WhenValidRequest() throws Exception {
        // ARRANGE: Service does nothing (success)
        doNothing().when(profileService).initiateRegistration(any(ProfileRequest.class));

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("name", TEST_NAME);
        requestBody.put("email", TEST_EMAIL);
        requestBody.put("password", TEST_PASSWORD);

        // ACT + ASSERT
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.email").value(TEST_EMAIL));
    }

    @Test
    @DisplayName("❌ POST /register - Should return 400 when email already exists")
    void register_Returns400_WhenEmailAlreadyExists() throws Exception {
        // ARRANGE: Service throws exception
        doThrow(new RuntimeException("Email already exists. Please login instead."))
                .when(profileService).initiateRegistration(any(ProfileRequest.class));

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("name", TEST_NAME);
        requestBody.put("email", TEST_EMAIL);
        requestBody.put("password", TEST_PASSWORD);

        // ACT + ASSERT
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email already exists. Please login instead."));
    }

    @Test
    @DisplayName("❌ POST /register - Should return 400 when name is blank")
    void register_Returns400_WhenNameBlank() throws Exception {
        // ARRANGE: @NotBlank validation will reject this
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("name", "");   // blank name
        requestBody.put("email", TEST_EMAIL);
        requestBody.put("password", TEST_PASSWORD);

        // ACT + ASSERT: Spring validation rejects before hitting controller
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest());
    }

    // =========================================================
    // ✅ TEST GROUP 2: POST /login - Login
    // =========================================================

    @Test
    @DisplayName("✅ POST /login - Should login successfully and return 200 with token")
    void login_Returns200_WithToken_WhenValidCredentials() throws Exception {
        // ARRANGE: Auth succeeds
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(TEST_EMAIL, TEST_PASSWORD));

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(sampleUser));

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("email", TEST_EMAIL);
        requestBody.put("password", TEST_PASSWORD);

        // ACT + ASSERT
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value(TEST_EMAIL));
    }

    @Test
    @DisplayName("❌ POST /login - Should return 400 when credentials are wrong")
    void login_Returns400_WhenBadCredentials() throws Exception {
        // ARRANGE: Auth throws BadCredentialsException
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("email", TEST_EMAIL);
        requestBody.put("password", "wrongpassword");

        // ACT + ASSERT
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(true))
                .andExpect(jsonPath("$.message").value("Email or password is incorrect"));
    }

    // =========================================================
    // ✅ TEST GROUP 3: POST /send-otp - Send OTP
    // =========================================================

    @Test
    @DisplayName("✅ POST /send-otp - Should send OTP successfully")
    void sendOtp_Returns200_WhenSuccess() throws Exception {
        // ARRANGE
        doNothing().when(profileService).sendRegistrationOtp(TEST_EMAIL);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("email", TEST_EMAIL);

        // ACT + ASSERT
        mockMvc.perform(post("/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("OTP sent successfully to your email"));
    }

    @Test
    @DisplayName("❌ POST /send-otp - Should return 400 when no pending registration")
    void sendOtp_Returns400_WhenNoPendingRegistration() throws Exception {
        // ARRANGE
        doThrow(new RuntimeException("Please complete registration first"))
                .when(profileService).sendRegistrationOtp(TEST_EMAIL);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("email", TEST_EMAIL);

        // ACT + ASSERT
        mockMvc.perform(post("/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("❌ POST /send-otp - Should return 400 when email is missing")
    void sendOtp_Returns400_WhenEmailMissing() throws Exception {
        // ARRANGE: Empty body
        Map<String, String> requestBody = new HashMap<>();

        // ACT + ASSERT
        mockMvc.perform(post("/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email is required"));
    }

    // =========================================================
    // ✅ TEST GROUP 4: POST /verify-otp - Verify OTP
    // =========================================================

    @Test
    @DisplayName("✅ POST /verify-otp - Should verify OTP and return token")
    void verifyOtp_Returns200_WithToken_WhenOtpCorrect() throws Exception {
        // ARRANGE
        doNothing().when(profileService).verifyRegistrationOtp(TEST_EMAIL, "123456");
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(sampleUser));

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("email", TEST_EMAIL);
        requestBody.put("otp", "123456");

        // ACT + ASSERT
        mockMvc.perform(post("/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value(TEST_EMAIL));
    }

    @Test
    @DisplayName("❌ POST /verify-otp - Should return 400 when OTP is wrong")
    void verifyOtp_Returns400_WhenOtpWrong() throws Exception {
        // ARRANGE
        doThrow(new RuntimeException("Invalid OTP. Please check and try again."))
                .when(profileService).verifyRegistrationOtp(TEST_EMAIL, "000000");

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("email", TEST_EMAIL);
        requestBody.put("otp", "000000");

        // ACT + ASSERT
        mockMvc.perform(post("/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid OTP. Please check and try again."));
    }

    @Test
    @DisplayName("❌ POST /verify-otp - Should return 400 when email missing")
    void verifyOtp_Returns400_WhenEmailMissing() throws Exception {
        // ARRANGE: No email in body
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("otp", "123456");

        // ACT + ASSERT
        mockMvc.perform(post("/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email is required"));
    }

    @Test
    @DisplayName("❌ POST /verify-otp - Should return 400 when OTP missing")
    void verifyOtp_Returns400_WhenOtpMissing() throws Exception {
        // ARRANGE: No OTP in body
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("email", TEST_EMAIL);

        // ACT + ASSERT
        mockMvc.perform(post("/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("OTP is required"));
    }

    // =========================================================
    // ✅ TEST GROUP 5: POST /logout - Logout
    // =========================================================

    @Test
    @DisplayName("✅ POST /logout - Should logout and return 200")
    void logout_Returns200_Always() throws Exception {
        // ACT + ASSERT: Logout is public - no token needed
        mockMvc.perform(post("/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logged out successfully"));
    }

    // =========================================================
    // ✅ TEST GROUP 6: POST /send-reset-otp - Password reset
    // =========================================================

    @Test
    @DisplayName("✅ POST /send-reset-otp - Should send reset OTP successfully")
    void sendResetOtp_Returns200_WhenUserExists() throws Exception {
        // ARRANGE
        doNothing().when(profileService).sendResetOtp(TEST_EMAIL);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("email", TEST_EMAIL);

        // ACT + ASSERT
        mockMvc.perform(post("/send-reset-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("❌ POST /send-reset-otp - Should return 400 when email missing")
    void sendResetOtp_Returns400_WhenEmailMissing() throws Exception {
        // ARRANGE: Empty body
        Map<String, String> requestBody = new HashMap<>();

        // ACT + ASSERT
        mockMvc.perform(post("/send-reset-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email is required"));
    }
}