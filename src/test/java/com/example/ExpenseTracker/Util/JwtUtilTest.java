package com.example.ExpenseTracker.Util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

// ✅ PART 4: JwtUtil Unit Tests
// 🔥 Pure JUnit - NO Mockito needed, NO Spring context, NO database
// 📁 File location: src/test/java/com/example/ExpenseTracker/Util/JwtUtilTest.java

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    // ✅ Real JwtUtil - we test the REAL logic, no mocking needed here
    private JwtUtil jwtUtil;

    // ✅ Test constants
    private UserDetails sampleUserDetails;
    private final String TEST_EMAIL = "seshathri@test.com";
    private final Long TEST_USER_ID = 1L;
    private final String TEST_NAME = "Seshathri";

    // ✅ Same secret key as your application.properties
    private final String TEST_SECRET_KEY = "thisisthesecretkeyievercreatedinmydevelopmentcareer";

    @BeforeEach
    void setUp() {
        // ✅ Create real JwtUtil instance
        jwtUtil = new JwtUtil();

        // ✅ Inject SECRET_KEY using ReflectionTestUtils
        // (because @Value doesn't work without Spring context)
        ReflectionTestUtils.setField(jwtUtil, "SECRET_KEY", TEST_SECRET_KEY);

        // ✅ Build a Spring Security UserDetails object
        sampleUserDetails = User.builder()
                .username(TEST_EMAIL)
                .password("encodedPassword")
                .authorities(Collections.emptyList())
                .build();
    }

    // =========================================================
    // ✅ TEST GROUP 1: generateToken() - Token generation
    // =========================================================

    @Test
    @DisplayName("✅ Should generate token with userId and name")
    void generateToken_WithUserIdAndName_Success() {
        // ACT: Generate token with all 3 params (main method you use)
        String token = jwtUtil.generateToken(sampleUserDetails, TEST_USER_ID, TEST_NAME);

        // ASSERT
        assertNotNull(token, "Token should not be null");
        assertFalse(token.isEmpty(), "Token should not be empty");
        // JWT format: header.payload.signature (3 parts separated by dots)
        assertEquals(3, token.split("\\.").length, "JWT must have 3 parts");
    }

    @Test
    @DisplayName("✅ Should generate token with userId only")
    void generateToken_WithUserIdOnly_Success() {
        // ACT
        String token = jwtUtil.generateToken(sampleUserDetails, TEST_USER_ID);

        // ASSERT
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(3, token.split("\\.").length, "JWT must have 3 parts");
    }

    @Test
    @DisplayName("✅ Should generate token with UserDetails only (backward compatibility)")
    void generateToken_WithUserDetailsOnly_Success() {
        // ACT
        String token = jwtUtil.generateToken(sampleUserDetails);

        // ASSERT
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    @DisplayName("✅ Should generate different tokens for different users")
    void generateToken_DifferentTokensForDifferentUsers() {
        // ARRANGE: Two different users
        UserDetails user1 = User.builder()
                .username("user1@test.com")
                .password("pass1")
                .authorities(Collections.emptyList())
                .build();

        UserDetails user2 = User.builder()
                .username("user2@test.com")
                .password("pass2")
                .authorities(Collections.emptyList())
                .build();

        // ACT
        String token1 = jwtUtil.generateToken(user1, 1L, "User One");
        String token2 = jwtUtil.generateToken(user2, 2L, "User Two");

        // ASSERT: Tokens must be different
        assertNotEquals(token1, token2, "Different users must get different tokens");
    }

    // =========================================================
    // ✅ TEST GROUP 2: extractEmail() - Email extraction
    // =========================================================

    @Test
    @DisplayName("✅ Should extract correct email from token")
    void extractEmail_ReturnsCorrectEmail() {
        // ARRANGE: Generate token first
        String token = jwtUtil.generateToken(sampleUserDetails, TEST_USER_ID, TEST_NAME);

        // ACT
        String extractedEmail = jwtUtil.extractEmail(token);

        // ASSERT
        assertNotNull(extractedEmail);
        assertEquals(TEST_EMAIL, extractedEmail, "Extracted email must match original");
    }

    @Test
    @DisplayName("✅ Should extract email from token generated with userId only")
    void extractEmail_FromTokenWithUserIdOnly() {
        // ARRANGE
        String token = jwtUtil.generateToken(sampleUserDetails, TEST_USER_ID);

        // ACT
        String extractedEmail = jwtUtil.extractEmail(token);

        // ASSERT
        assertEquals(TEST_EMAIL, extractedEmail);
    }

    @Test
    @DisplayName("✅ Should extract correct email for different users")
    void extractEmail_CorrectForDifferentUsers() {
        // ARRANGE
        String email1 = "user1@test.com";
        String email2 = "user2@test.com";

        UserDetails user1 = User.builder()
                .username(email1)
                .password("pass")
                .authorities(Collections.emptyList())
                .build();

        UserDetails user2 = User.builder()
                .username(email2)
                .password("pass")
                .authorities(Collections.emptyList())
                .build();

        String token1 = jwtUtil.generateToken(user1, 1L, "User One");
        String token2 = jwtUtil.generateToken(user2, 2L, "User Two");

        // ACT + ASSERT
        assertEquals(email1, jwtUtil.extractEmail(token1), "Email 1 must match");
        assertEquals(email2, jwtUtil.extractEmail(token2), "Email 2 must match");
    }

    // =========================================================
    // ✅ TEST GROUP 3: extractUserId() - UserId extraction
    // =========================================================

    @Test
    @DisplayName("✅ Should extract correct userId from token")
    void extractUserId_ReturnsCorrectUserId() {
        // ARRANGE
        String token = jwtUtil.generateToken(sampleUserDetails, TEST_USER_ID, TEST_NAME);

        // ACT
        Long extractedUserId = jwtUtil.extractUserId(token);

        // ASSERT
        assertNotNull(extractedUserId, "UserId should not be null");
        assertEquals(TEST_USER_ID, extractedUserId, "UserId must match original");
    }

    @Test
    @DisplayName("✅ Should extract correct userId for different users")
    void extractUserId_CorrectForDifferentUsers() {
        // ARRANGE
        Long userId1 = 101L;
        Long userId2 = 202L;

        String token1 = jwtUtil.generateToken(sampleUserDetails, userId1, "User One");

        UserDetails user2 = User.builder()
                .username("user2@test.com")
                .password("pass")
                .authorities(Collections.emptyList())
                .build();
        String token2 = jwtUtil.generateToken(user2, userId2, "User Two");

        // ACT + ASSERT
        assertEquals(userId1, jwtUtil.extractUserId(token1), "UserId 101 must match");
        assertEquals(userId2, jwtUtil.extractUserId(token2), "UserId 202 must match");
    }

    @Test
    @DisplayName("✅ Should return null userId from token generated without userId")
    void extractUserId_ReturnsNull_WhenTokenHasNoUserId() {
        // ARRANGE: Token generated WITHOUT userId claim
        String token = jwtUtil.generateToken(sampleUserDetails);

        // ACT
        Long extractedUserId = jwtUtil.extractUserId(token);

        // ASSERT: Should return null gracefully (not throw exception)
        assertNull(extractedUserId, "UserId should be null when not in token");
    }

    // =========================================================
    // ✅ TEST GROUP 4: extractName() - Name extraction
    // =========================================================

    @Test
    @DisplayName("✅ Should extract correct name from token")
    void extractName_ReturnsCorrectName() {
        // ARRANGE
        String token = jwtUtil.generateToken(sampleUserDetails, TEST_USER_ID, TEST_NAME);

        // ACT
        String extractedName = jwtUtil.extractName(token);

        // ASSERT
        assertNotNull(extractedName);
        assertEquals(TEST_NAME, extractedName, "Extracted name must match original");
    }

    @Test
    @DisplayName("✅ Should extract correct name for different users")
    void extractName_CorrectForDifferentUsers() {
        // ARRANGE
        String name1 = "Seshathri";
        String name2 = "Priya";

        UserDetails user2 = User.builder()
                .username("priya@test.com")
                .password("pass")
                .authorities(Collections.emptyList())
                .build();

        String token1 = jwtUtil.generateToken(sampleUserDetails, 1L, name1);
        String token2 = jwtUtil.generateToken(user2, 2L, name2);

        // ACT + ASSERT
        assertEquals(name1, jwtUtil.extractName(token1), "Name 1 must match");
        assertEquals(name2, jwtUtil.extractName(token2), "Name 2 must match");
    }

    @Test
    @DisplayName("✅ Should return null name from token generated without name")
    void extractName_ReturnsNull_WhenTokenHasNoName() {
        // ARRANGE: Token generated WITHOUT name claim
        String token = jwtUtil.generateToken(sampleUserDetails, TEST_USER_ID);

        // ACT
        String extractedName = jwtUtil.extractName(token);

        // ASSERT: Null gracefully, no exception
        assertNull(extractedName, "Name should be null when not in token");
    }

    // =========================================================
    // ✅ TEST GROUP 5: validateToken() - Token validation
    // =========================================================

    @Test
    @DisplayName("✅ Should validate token successfully for correct user")
    void validateToken_ReturnsTrue_ForValidToken() {
        // ARRANGE
        String token = jwtUtil.generateToken(sampleUserDetails, TEST_USER_ID, TEST_NAME);

        // ACT
        Boolean isValid = jwtUtil.validateToken(token, sampleUserDetails);

        // ASSERT
        assertTrue(isValid, "Token should be valid for correct user");
    }

    @Test
    @DisplayName("❌ Should return false when token email doesn't match UserDetails")
    void validateToken_ReturnsFalse_WhenEmailMismatch() {
        // ARRANGE: Token generated for user1, but validated against user2
        String token = jwtUtil.generateToken(sampleUserDetails, TEST_USER_ID, TEST_NAME);

        // Different UserDetails (different email)
        UserDetails differentUser = User.builder()
                .username("different@test.com")
                .password("pass")
                .authorities(Collections.emptyList())
                .build();

        // ACT
        Boolean isValid = jwtUtil.validateToken(token, differentUser);

        // ASSERT
        assertFalse(isValid, "Token should NOT be valid for different user");
    }

    @Test
    @DisplayName("✅ Should validate tokens for multiple different users correctly")
    void validateToken_CorrectForMultipleUsers() {
        // ARRANGE
        UserDetails user1 = User.builder()
                .username("user1@test.com")
                .password("pass1")
                .authorities(Collections.emptyList())
                .build();

        UserDetails user2 = User.builder()
                .username("user2@test.com")
                .password("pass2")
                .authorities(Collections.emptyList())
                .build();

        String token1 = jwtUtil.generateToken(user1, 1L, "User One");
        String token2 = jwtUtil.generateToken(user2, 2L, "User Two");

        // ACT + ASSERT
        assertTrue(jwtUtil.validateToken(token1, user1), "Token1 valid for user1");
        assertTrue(jwtUtil.validateToken(token2, user2), "Token2 valid for user2");
        // Cross validation must FAIL
        assertFalse(jwtUtil.validateToken(token1, user2), "Token1 must NOT be valid for user2");
        assertFalse(jwtUtil.validateToken(token2, user1), "Token2 must NOT be valid for user1");
    }

    // =========================================================
    // ✅ TEST GROUP 6: Full token round-trip tests
    // =========================================================

    @Test
    @DisplayName("✅ Full round-trip: Generate → Extract email, userId, name → Validate")
    void fullRoundTrip_GenerateAndExtractAllClaims() {
        // ARRANGE
        String expectedEmail = TEST_EMAIL;
        Long expectedUserId = 42L;
        String expectedName = "Seshathri";

        // ACT: Generate token
        String token = jwtUtil.generateToken(sampleUserDetails, expectedUserId, expectedName);

        // ASSERT: Extract all claims and verify
        assertEquals(expectedEmail, jwtUtil.extractEmail(token),
                "Email must match after extraction");
        assertEquals(expectedUserId, jwtUtil.extractUserId(token),
                "UserId must match after extraction");
        assertEquals(expectedName, jwtUtil.extractName(token),
                "Name must match after extraction");
        assertTrue(jwtUtil.validateToken(token, sampleUserDetails),
                "Token must be valid");
    }

    @Test
    @DisplayName("✅ Token should contain correct email as subject")
    void generateToken_EmailIsSubject() {
        // ARRANGE
        String token = jwtUtil.generateToken(sampleUserDetails, TEST_USER_ID, TEST_NAME);

        // ACT: Extract email (which is JWT subject)
        String subject = jwtUtil.extractEmail(token);

        // ASSERT
        assertEquals(TEST_EMAIL, subject,
                "Email should be stored as JWT subject");
    }

    @Test
    @DisplayName("✅ Large userId should be extracted correctly")
    void extractUserId_HandlesLargeUserId() {
        // ARRANGE: Large userId (real world scenario)
        Long largeUserId = 999999L;
        String token = jwtUtil.generateToken(sampleUserDetails, largeUserId, TEST_NAME);

        // ACT
        Long extracted = jwtUtil.extractUserId(token);

        // ASSERT
        assertEquals(largeUserId, extracted,
                "Large userId must be extracted correctly without overflow");
    }
}